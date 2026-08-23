import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { MenuResponse } from '../../../core/catalogos/menu';
import { MenuService } from '../../../core/catalogos/menu.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { CotizacionService } from '../cotizacion.service';
import {
  CotizacionResponse,
  CotizacionVersionResponse,
  DetalleCotizacionResponse,
  TRANSICIONES_VALIDAS,
} from '../dto/cotizacion';

const PAGINA_URL = '/api/cotizaciones';
const TIPO_ESTADO_COTIZACION = 'COTIZACION';

@Component({
  selector: 'app-version-detalle',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
  ],
  templateUrl: './version-detalle.html',
  styleUrl: './version-detalle.scss',
})
export class VersionDetalle implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly cotizacionService = inject(CotizacionService);
  private readonly menuService = inject(MenuService);
  private readonly estadoService = inject(EstadoService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly idCotizacion: number;
  readonly idVersion: number;

  readonly cotizacion = signal<CotizacionResponse | null>(null);
  readonly version = signal<CotizacionVersionResponse | null>(null);
  readonly detalles = signal<DetalleCotizacionResponse[]>([]);

  /** Solo informativo: no bloquea nada, avisa si el total ya supera lo que el cliente dijo que queria gastar. */
  readonly presupuestoSuperado = computed(() => {
    const presupuesto = this.cotizacion()?.presupuestoCliente;
    const total = this.version()?.montoTotal;
    return presupuesto != null && total != null && total > presupuesto;
  });
  readonly menus = signal<MenuResponse[]>([]);
  readonly estadosCotizacion = signal<EstadoResponse[]>([]);
  readonly idDetalleEditando = signal<number | null>(null);

  readonly columnas = ['menu', 'cantidad', 'precio', 'subtotal', 'acciones'];

  readonly formulario = this.crearFormulario();

  constructor() {
    this.idCotizacion = Number(this.route.snapshot.paramMap.get('id'));
    this.idVersion = Number(this.route.snapshot.paramMap.get('versionId'));
  }

  private crearFormulario() {
    return this.fb.nonNullable.group({
      idMenu: this.fb.control<number | null>(null, Validators.required),
      cantidadPlatos: [1, [Validators.required, Validators.min(1)]],
      precioUnitario: this.fb.control<number | null>(null, Validators.required),
      observaciones: [''],
    });
  }

  get esEditable(): boolean {
    return this.version()?.estadoNombre === 'CREADA';
  }

  get puedeModificar(): boolean {
    return this.authService.tienePermiso(PAGINA_URL, 'modificacion');
  }

  get puedeImprimir(): boolean {
    return this.authService.tienePermiso(PAGINA_URL, 'imprimir');
  }

  get transicionesDisponibles(): EstadoResponse[] {
    const actual = this.version()?.estadoNombre;
    if (!actual) {
      return [];
    }
    const permitidos = TRANSICIONES_VALIDAS[actual] ?? [];
    return this.estadosCotizacion().filter((e) => permitidos.includes(e.nombre));
  }

  ngOnInit(): void {
    this.menuService.listarActivos().subscribe((menus) => this.menus.set(menus));
    this.estadoService.listarPorTipo(TIPO_ESTADO_COTIZACION).subscribe((estados) => this.estadosCotizacion.set(estados));
    this.cargar();
  }

  cargar(): void {
    this.cotizacionService.obtener(this.idCotizacion).subscribe((c) => this.cotizacion.set(c));
    this.cotizacionService.obtenerVersion(this.idVersion).subscribe((v) => this.version.set(v));
    this.cotizacionService.listarDetalle(this.idVersion).subscribe((d) => this.detalles.set(d));
  }

  editarLinea(detalle: DetalleCotizacionResponse): void {
    this.idDetalleEditando.set(detalle.idDetalleCotizacion);
    this.formulario.setValue({
      idMenu: detalle.idMenu,
      cantidadPlatos: detalle.cantidadPlatos,
      precioUnitario: detalle.precioUnitario,
      observaciones: detalle.observaciones ?? '',
    });
  }

  cancelarEdicion(): void {
    this.idDetalleEditando.set(null);
    this.formulario.reset({ idMenu: null, cantidadPlatos: 1, precioUnitario: null, observaciones: '' });
  }

  guardarLinea(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();
    const request = {
      idMenu: valores.idMenu!,
      cantidadPlatos: valores.cantidadPlatos,
      precioUnitario: valores.precioUnitario!,
      observaciones: valores.observaciones || null,
    };

    const idEditando = this.idDetalleEditando();
    const operacion = idEditando
      ? this.cotizacionService.actualizarDetalle(this.idVersion, idEditando, request)
      : this.cotizacionService.agregarDetalle(this.idVersion, request);

    operacion.subscribe(() => {
      this.snackBar.open(idEditando ? 'Linea actualizada' : 'Linea agregada', 'Cerrar', { duration: 3000 });
      this.cancelarEdicion();
      this.cargar();
    });
  }

  eliminarLinea(detalle: DetalleCotizacionResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Eliminar', mensaje: `Quitar "${detalle.nombreMenu}" de la cotización?` },
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.cotizacionService.eliminarDetalle(this.idVersion, detalle.idDetalleCotizacion).subscribe(() => {
        this.snackBar.open('Linea eliminada', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }

  cambiarEstado(estado: EstadoResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Cambiar estado', mensaje: `¿Pasar esta version a "${estado.nombre}"?` },
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.cotizacionService.cambiarEstadoVersion(this.idVersion, estado.idEstado).subscribe(() => {
        this.snackBar.open('Estado actualizado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }

  descargarPdf(): void {
    this.cotizacionService.descargarPdf(this.idVersion).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const enlace = document.createElement('a');
      enlace.href = url;
      enlace.download = `cotizacion-${this.idVersion}.pdf`;
      enlace.click();
      URL.revokeObjectURL(url);
    });
  }
}
