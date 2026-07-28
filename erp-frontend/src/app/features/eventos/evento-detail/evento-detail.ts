import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
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
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { ProductoResponse } from '../../../core/catalogos/producto';
import { ProductoService } from '../../../core/catalogos/producto.service';
import { TipoCostoResponse } from '../../../core/catalogos/tipo-costo';
import { TipoCostoService } from '../../../core/catalogos/tipo-costo.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { EmpleadoResponse } from '../../empleados/dto/empleado';
import { EmpleadoService } from '../../empleados/empleado.service';
import { VehiculoResponse } from '../../vehiculos/dto/vehiculo';
import { VehiculoService } from '../../vehiculos/vehiculo.service';
import {
  CostoEventoResponse,
  EventoEmpleadoResponse,
  EventoInventarioResponse,
  EventoResponse,
  EventoVehiculoResponse,
  TRANSICIONES_VALIDAS_EVENTO,
} from '../dto/evento';
import { EventoService } from '../evento.service';

const PAGINA_URL = '/api/eventos';
const TIPO_ESTADO_EVENTO = 'EVENTO';

@Component({
  selector: 'app-evento-detail',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTabsModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
  ],
  templateUrl: './evento-detail.html',
  styleUrl: './evento-detail.scss',
})
export class EventoDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly eventoService = inject(EventoService);
  private readonly empleadoService = inject(EmpleadoService);
  private readonly vehiculoService = inject(VehiculoService);
  private readonly productoService = inject(ProductoService);
  private readonly tipoCostoService = inject(TipoCostoService);
  private readonly estadoService = inject(EstadoService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly idEvento: number;
  readonly evento = signal<EventoResponse | null>(null);
  readonly costos = signal<CostoEventoResponse[]>([]);
  readonly personal = signal<EventoEmpleadoResponse[]>([]);
  readonly vehiculos = signal<EventoVehiculoResponse[]>([]);
  readonly inventario = signal<EventoInventarioResponse[]>([]);

  readonly empleadosDisponibles = signal<EmpleadoResponse[]>([]);
  readonly vehiculosDisponibles = signal<VehiculoResponse[]>([]);
  readonly productosDisponibles = signal<ProductoResponse[]>([]);
  readonly tiposCosto = signal<TipoCostoResponse[]>([]);
  readonly estadosEvento = signal<EstadoResponse[]>([]);

  readonly columnasCostos = ['tipo', 'descripcion', 'monto', 'fecha', 'acciones'];
  readonly columnasPersonal = ['empleado', 'salario', 'horario', 'acciones'];
  readonly columnasVehiculos = ['placa', 'conductor', 'acciones'];
  readonly columnasInventario = ['producto', 'cantidad', 'consumo', 'acciones'];

  readonly formularioCosto = this.fb.nonNullable.group({
    idTipoCosto: this.fb.control<number | null>(null, Validators.required),
    descripcion: [''],
    monto: this.fb.control<number | null>(null, Validators.required),
  });

  readonly formularioPersonal = this.fb.nonNullable.group({
    idEmpleado: this.fb.control<number | null>(null, Validators.required),
    salarioEvento: this.fb.control<number | null>(null, Validators.required),
    horaInicio: [''],
    horaFin: [''],
  });

  readonly formularioVehiculo = this.fb.nonNullable.group({
    idVehiculo: this.fb.control<number | null>(null, Validators.required),
    idEmpleadoConductor: this.fb.control<number | null>(null),
  });

  readonly formularioInventario = this.fb.nonNullable.group({
    idProducto: this.fb.control<number | null>(null, Validators.required),
    cantidad: this.fb.control<number | null>(null, Validators.required),
  });

  constructor() {
    this.idEvento = Number(this.route.snapshot.paramMap.get('id'));
  }

  get puedeModificar(): boolean {
    return this.authService.tienePermiso(PAGINA_URL, 'modificacion');
  }

  get puedeCancelar(): boolean {
    return this.authService.esAdministrador();
  }

  get transicionesDisponibles(): EstadoResponse[] {
    const actual = this.evento()?.estadoNombre;
    if (!actual) {
      return [];
    }
    const permitidos = TRANSICIONES_VALIDAS_EVENTO[actual] ?? [];
    return this.estadosEvento().filter(
      (e) => permitidos.includes(e.nombre) && (e.nombre !== 'CANCELADO' || this.puedeCancelar),
    );
  }

  ngOnInit(): void {
    this.empleadoService.listar('', 0, 200).subscribe((p) => this.empleadosDisponibles.set(p.content));
    this.vehiculoService.listar(0, 200).subscribe((p) => this.vehiculosDisponibles.set(p.content));
    this.productoService.listarTodos().subscribe((p) => this.productosDisponibles.set(p));
    this.tipoCostoService.listar().subscribe((t) => this.tiposCosto.set(t));
    this.estadoService.listarPorTipo(TIPO_ESTADO_EVENTO).subscribe((e) => this.estadosEvento.set(e));
    this.cargar();
  }

  cargar(): void {
    this.eventoService.obtener(this.idEvento).subscribe((e) => this.evento.set(e));
    this.eventoService.listarCostos(this.idEvento).subscribe((c) => this.costos.set(c));
    this.eventoService.listarPersonal(this.idEvento).subscribe((p) => this.personal.set(p));
    this.eventoService.listarVehiculos(this.idEvento).subscribe((v) => this.vehiculos.set(v));
    this.eventoService.listarInventario(this.idEvento).subscribe((i) => this.inventario.set(i));
  }

  cambiarEstado(estado: EstadoResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: { titulo: 'Cambiar estado', mensaje: `¿Pasar este evento a "${estado.nombre}"?` },
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.eventoService.cambiarEstado(this.idEvento, estado.idEstado).subscribe(() => {
        this.snackBar.open('Estado actualizado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }

  // --- Costos ---

  agregarCosto(): void {
    if (this.formularioCosto.invalid) {
      this.formularioCosto.markAllAsTouched();
      return;
    }
    const v = this.formularioCosto.getRawValue();
    this.eventoService
      .agregarCosto(this.idEvento, { idTipoCosto: v.idTipoCosto!, descripcion: v.descripcion || null, monto: v.monto!, fechaCosto: null })
      .subscribe(() => {
        this.formularioCosto.reset();
        this.cargar();
      });
  }

  eliminarCosto(costo: CostoEventoResponse): void {
    this.eventoService.eliminarCosto(this.idEvento, costo.idCostoEvento).subscribe(() => this.cargar());
  }

  // --- Personal ---

  asignarEmpleado(): void {
    if (this.formularioPersonal.invalid) {
      this.formularioPersonal.markAllAsTouched();
      return;
    }
    const v = this.formularioPersonal.getRawValue();
    this.eventoService
      .asignarEmpleado(this.idEvento, v.idEmpleado!, {
        salarioEvento: v.salarioEvento!,
        horaInicio: v.horaInicio || null,
        horaFin: v.horaFin || null,
        idEstado: null,
      })
      .subscribe(() => {
        this.formularioPersonal.reset();
        this.cargar();
      });
  }

  quitarEmpleado(asignacion: EventoEmpleadoResponse): void {
    this.eventoService.quitarEmpleado(this.idEvento, asignacion.idEmpleado).subscribe(() => this.cargar());
  }

  // --- Vehiculos ---

  asignarVehiculo(): void {
    if (this.formularioVehiculo.invalid) {
      this.formularioVehiculo.markAllAsTouched();
      return;
    }
    const v = this.formularioVehiculo.getRawValue();
    this.eventoService
      .asignarVehiculo(this.idEvento, v.idVehiculo!, { idEmpleadoConductor: v.idEmpleadoConductor })
      .subscribe(() => {
        this.formularioVehiculo.reset();
        this.cargar();
      });
  }

  quitarVehiculo(asignacion: EventoVehiculoResponse): void {
    this.eventoService.quitarVehiculo(this.idEvento, asignacion.idVehiculo).subscribe(() => this.cargar());
  }

  // --- Inventario ---

  planificarProducto(): void {
    if (this.formularioInventario.invalid) {
      this.formularioInventario.markAllAsTouched();
      return;
    }
    const v = this.formularioInventario.getRawValue();
    this.eventoService
      .planificarProducto(this.idEvento, v.idProducto!, { cantidad: v.cantidad!, fechaConsumo: null })
      .subscribe(() => {
        this.formularioInventario.reset();
        this.cargar();
      });
  }

  quitarProducto(item: EventoInventarioResponse): void {
    this.eventoService.quitarProducto(this.idEvento, item.idProducto).subscribe(() => this.cargar());
  }

  confirmarConsumo(item: EventoInventarioResponse): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: {
        titulo: 'Confirmar consumo',
        mensaje: `¿Confirmar que se consumieron ${item.cantidad} de "${item.nombreProducto}"? Esto descuenta el stock real.`,
      },
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.eventoService.confirmarConsumo(this.idEvento, item.idProducto).subscribe(() => {
        this.snackBar.open('Consumo confirmado, stock actualizado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
    });
  }
}
