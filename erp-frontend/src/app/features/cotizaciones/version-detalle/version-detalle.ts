import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize, timer, zip } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { MenuPlatoResponse, MenuResponse } from '../../../core/catalogos/menu';
import { MenuService } from '../../../core/catalogos/menu.service';
import { TipoServicioResponse } from '../../../core/catalogos/tipo-servicio';
import { TipoServicioService } from '../../../core/catalogos/tipo-servicio.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { CotizacionService } from '../cotizacion.service';
import {
  CotizacionResponse,
  CotizacionVersionResponse,
  DetalleCotizacionResponse,
  ServicioCotizacionResponse,
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
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './version-detalle.html',
  styleUrl: './version-detalle.scss',
})
export class VersionDetalle implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly cotizacionService = inject(CotizacionService);
  private readonly menuService = inject(MenuService);
  private readonly tipoServicioService = inject(TipoServicioService);
  private readonly estadoService = inject(EstadoService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly idCotizacion: number;
  readonly idVersion: number;

  readonly cotizacion = signal<CotizacionResponse | null>(null);
  readonly version = signal<CotizacionVersionResponse | null>(null);
  readonly detalles = signal<DetalleCotizacionResponse[]>([]);

  /**
   * Solo informativo: cuanto queda el total arriba (positivo) o abajo (negativo) de lo que
   * el cliente dijo que queria gastar. null si no dio presupuesto.
   */
  readonly diferenciaPresupuesto = computed(() => {
    const presupuesto = this.cotizacion()?.presupuestoCliente;
    const total = this.version()?.montoTotal;
    return presupuesto != null && total != null ? total - presupuesto : null;
  });

  /**
   * Solo informativo: menus cuyas porciones no alcanzan o sobran para las personas de la
   * cotizacion. No bloquea, porque hay casos validos (menu infantil, platos para compartir).
   */
  readonly porcionesDescuadradas = computed(() => {
    const personas = this.cotizacion()?.cantidadPersonas;
    if (!personas) {
      return [];
    }
    const porMenu = new Map<string, number>();
    for (const d of this.detalles()) {
      porMenu.set(d.nombreMenu, (porMenu.get(d.nombreMenu) ?? 0) + d.cantidadPlatos);
    }
    return [...porMenu]
      .filter(([, porciones]) => porciones !== personas)
      .map(([menu, porciones]) => ({ menu, porciones, personas }));
  });
  readonly menus = signal<MenuResponse[]>([]);
  /** Platos disponibles dentro del menu (categoria) elegido en el formulario. */
  readonly platosDelMenu = signal<MenuPlatoResponse[]>([]);
  readonly precioSeleccionado = signal<number | null>(null);
  readonly estadosCotizacion = signal<EstadoResponse[]>([]);
  readonly idDetalleEditando = signal<number | null>(null);
  readonly descargandoPdf = signal(false);

  readonly servicios = signal<ServicioCotizacionResponse[]>([]);
  readonly tiposServicio = signal<TipoServicioResponse[]>([]);
  readonly idServicioEditando = signal<number | null>(null);

  readonly columnas = ['menu', 'cantidad', 'precio', 'subtotal', 'acciones'];
  readonly columnasServicios = ['tipo', 'descripcion', 'monto', 'acciones'];

  readonly formulario = this.crearFormulario();
  readonly formularioServicio = this.crearFormularioServicio();

  constructor() {
    this.idCotizacion = Number(this.route.snapshot.paramMap.get('id'));
    this.idVersion = Number(this.route.snapshot.paramMap.get('versionId'));
  }

  private crearFormulario() {
    return this.fb.nonNullable.group({
      idMenu: this.fb.control<number | null>(null, Validators.required),
      idPlato: this.fb.control<number | null>(null, Validators.required),
      cantidadPlatos: [1, [Validators.required, Validators.min(1)]],
      observaciones: [''],
    });
  }

  private crearFormularioServicio() {
    return this.fb.nonNullable.group({
      idTipoServicio: this.fb.control<number | null>(null, Validators.required),
      descripcion: [''],
      monto: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
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
    this.tipoServicioService.listar().subscribe((tipos) => this.tiposServicio.set(tipos));
    this.estadoService.listarPorTipo(TIPO_ESTADO_COTIZACION).subscribe((estados) => this.estadosCotizacion.set(estados));
    this.cargar();

    this.formulario.controls.idMenu.valueChanges.subscribe((idMenu) => {
      this.formulario.controls.idPlato.setValue(null);
      this.precioSeleccionado.set(null);
      if (idMenu == null) {
        this.platosDelMenu.set([]);
        return;
      }
      this.menuService.listarPlatosDeMenu(idMenu).subscribe((platos) => this.platosDelMenu.set(platos));
    });

    this.formulario.controls.idPlato.valueChanges.subscribe((idPlato) => {
      const plato = this.platosDelMenu().find((p) => p.idPlato === idPlato);
      this.precioSeleccionado.set(plato?.precioUnitario ?? null);
    });
  }

  cargar(): void {
    this.cotizacionService.obtener(this.idCotizacion).subscribe((c) => this.cotizacion.set(c));
    this.cotizacionService.obtenerVersion(this.idVersion).subscribe((v) => this.version.set(v));
    this.cotizacionService.listarDetalle(this.idVersion).subscribe((d) => this.detalles.set(d));
    this.cotizacionService.listarServicios(this.idVersion).subscribe((s) => this.servicios.set(s));
  }

  editarLinea(detalle: DetalleCotizacionResponse): void {
    this.idDetalleEditando.set(detalle.idDetalleCotizacion);
    this.menuService.listarPlatosDeMenu(detalle.idMenu).subscribe((platos) => {
      this.platosDelMenu.set(platos);
      this.formulario.setValue(
        {
          idMenu: detalle.idMenu,
          idPlato: detalle.idPlato,
          cantidadPlatos: detalle.cantidadPlatos,
          observaciones: detalle.observaciones ?? '',
        },
        { emitEvent: false },
      );
      this.precioSeleccionado.set(detalle.precioUnitario);
    });
  }

  cancelarEdicion(): void {
    this.idDetalleEditando.set(null);
    this.platosDelMenu.set([]);
    this.precioSeleccionado.set(null);
    this.formulario.reset({ idMenu: null, idPlato: null, cantidadPlatos: 1, observaciones: '' });
  }

  guardarLinea(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();
    const request = {
      idMenu: valores.idMenu!,
      idPlato: valores.idPlato!,
      cantidadPlatos: valores.cantidadPlatos,
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
      data: { titulo: 'Eliminar', mensaje: `Quitar "${detalle.nombrePlato}" de la cotización?` },
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

  // --- Servicios extra (bebidas, decoracion, personal, etc.) ---

  editarServicio(servicio: ServicioCotizacionResponse): void {
    this.idServicioEditando.set(servicio.idServicioCotizacion);
    this.formularioServicio.setValue({
      idTipoServicio: servicio.idTipoServicio,
      descripcion: servicio.descripcion ?? '',
      monto: servicio.monto,
    });
  }

  cancelarEdicionServicio(): void {
    this.idServicioEditando.set(null);
    this.formularioServicio.reset({ idTipoServicio: null, descripcion: '', monto: null });
  }

  guardarServicio(): void {
    if (this.formularioServicio.invalid) {
      this.formularioServicio.markAllAsTouched();
      return;
    }

    const valores = this.formularioServicio.getRawValue();
    const request = {
      idTipoServicio: valores.idTipoServicio!,
      descripcion: valores.descripcion || null,
      monto: valores.monto!,
    };

    const idEditando = this.idServicioEditando();
    const operacion = idEditando
      ? this.cotizacionService.actualizarServicio(this.idVersion, idEditando, request)
      : this.cotizacionService.agregarServicio(this.idVersion, request);

    operacion.subscribe(() => {
      this.snackBar.open(idEditando ? 'Servicio actualizado' : 'Servicio agregado', 'Cerrar', { duration: 3000 });
      this.cancelarEdicionServicio();
      this.cargar();
    });
  }

  eliminarServicio(servicio: ServicioCotizacionResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Eliminar', mensaje: `¿Quitar "${servicio.tipoServicioNombre}" de la cotización?` },
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.cotizacionService.eliminarServicio(this.idVersion, servicio.idServicioCotizacion).subscribe(() => {
        this.snackBar.open('Servicio eliminado', 'Cerrar', { duration: 3000 });
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
    this.descargandoPdf.set(true);
    // Espera minima para que el spinner sea perceptible incluso si el PDF llega al instante.
    zip(this.cotizacionService.descargarPdf(this.idVersion), timer(500))
      .pipe(finalize(() => this.descargandoPdf.set(false)))
      .subscribe(([blob]) => {
        const url = URL.createObjectURL(blob);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `cotizacion-${this.idVersion}.pdf`;
        enlace.click();
        URL.revokeObjectURL(url);
      });
  }
}
