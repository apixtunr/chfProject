import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { VerPdf } from '../../../shared/reporte/exportar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { interval, of, switchMap } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import {
  MenuPlatoResponse,
  MenuResponse,
  PERSONAS_PRECIO_VOLUMEN,
  PRECIO_POR,
  precioPorUnidad,
} from '../../../core/catalogos/menu';
import { MenuService } from '../../../core/catalogos/menu.service';
import { ProductoResponse } from '../../../core/catalogos/producto';
import { ProductoService } from '../../../core/catalogos/producto.service';
import { TipoCostoResponse } from '../../../core/catalogos/tipo-costo';
import { TipoCostoService } from '../../../core/catalogos/tipo-costo.service';
import { BebidaResumen } from '../../../core/catalogos/bebida';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { CancelarEventoDialog, CancelarEventoDialogData } from '../cancelar-evento-dialog/cancelar-evento-dialog';
import { EmpleadoResponse } from '../../empleados/dto/empleado';
import { EmpleadoService } from '../../empleados/empleado.service';
import { VehiculoResponse } from '../../vehiculos/dto/vehiculo';
import { VehiculoService } from '../../vehiculos/vehiculo.service';
import { MetodoPagoResponse } from '../../pagos/dto/pago';
import { PagoService } from '../../pagos/pago.service';
import { CotizacionService } from '../../cotizaciones/cotizacion.service';
import { DetalleCotizacionResponse, ServicioCotizacionResponse } from '../../cotizaciones/dto/cotizacion';
import {
  ACUERDOS_ANTICIPO,
  AcuerdoAnticipo,
  CancelarEventoRequest,
  ColorEstado,
  COLOR_ESTADO_EVENTO_DEFECTO,
  COLOR_POR_ESTADO_EVENTO,
  CostoEventoResponse,
  DetalleEventoResponse,
  EventoEmpleadoResponse,
  EventoInventarioResponse,
  EventoResponse,
  EventoVehiculoResponse,
} from '../dto/evento';
import { EventoService } from '../evento.service';
import { TablaResponsiva } from '../../../shared/tabla-responsiva';

const PAGINA_URL = '/api/eventos';

@Component({
  selector: 'app-evento-detail',
  imports: [TablaResponsiva, 
    CommonModule,
    RouterLink,
    MatCardModule,
    ReactiveFormsModule,
    MatTabsModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
    MatDatepickerModule,
    MatCheckboxModule,
    MatTooltipModule,
  ],
  templateUrl: './evento-detail.html',
  styleUrl: './evento-detail.scss',
})
export class EventoDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  private readonly eventoService = inject(EventoService);
  private readonly empleadoService = inject(EmpleadoService);
  private readonly vehiculoService = inject(VehiculoService);
  private readonly productoService = inject(ProductoService);
  private readonly tipoCostoService = inject(TipoCostoService);
  private readonly menuService = inject(MenuService);
  private readonly cotizacionService = inject(CotizacionService);
  private readonly pagoService = inject(PagoService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly verPdf = inject(VerPdf);
  private readonly snackBar = inject(MatSnackBar);

  readonly idEvento: number;
  readonly evento = signal<EventoResponse | null>(null);
  readonly costos = signal<CostoEventoResponse[]>([]);
  readonly personal = signal<EventoEmpleadoResponse[]>([]);

  readonly hayConductores = computed(() => this.personal().some((p) => p.tieneLicencia));
  readonly vehiculos = signal<EventoVehiculoResponse[]>([]);
  readonly inventario = signal<EventoInventarioResponse[]>([]);
  readonly detalleMenu = signal<DetalleEventoResponse[]>([]);
  readonly detalleCotizacionOrigen = signal<DetalleCotizacionResponse[]>([]);
  readonly serviciosCotizacionOrigen = signal<ServicioCotizacionResponse[]>([]);

  readonly empleadosDisponibles = signal<EmpleadoResponse[]>([]);
  readonly vehiculosDisponibles = signal<VehiculoResponse[]>([]);

  readonly empleadosParaAsignar = computed(() => {
    const asignados = new Set(this.personal().map((p) => p.idEmpleado));
    return this.empleadosDisponibles().filter(
      (e) => e.estadoNombre === 'ACTIVO' && !asignados.has(e.idEmpleado),
    );
  });

  readonly vehiculosParaAsignar = computed(() => {
    const asignados = new Set(this.vehiculos().map((v) => v.idVehiculo));
    return this.vehiculosDisponibles().filter((v) => !asignados.has(v.idVehiculo));
  });
  readonly productosDisponibles = signal<ProductoResponse[]>([]);
  readonly tiposCosto = signal<TipoCostoResponse[]>([]);
  readonly metodosPago = signal<MetodoPagoResponse[]>([]);
  readonly menusDisponibles = signal<MenuResponse[]>([]);
  readonly platosDelMenu = signal<MenuPlatoResponse[]>([]);
  readonly precioSeleccionado = signal<number | null>(null);

  readonly columnasCostos = ['tipo', 'descripcion', 'monto', 'fecha', 'pagado', 'acciones'];
  readonly columnasPersonal = ['empleado', 'salario', 'horario', 'acciones'];
  readonly columnasVehiculos = ['placa', 'conductor', 'acciones'];
  private readonly columnasInventarioBase = ['producto', 'cantidad', 'consumo', 'fechaConfirmacion'];
  readonly columnasMenu = ['menu', 'bebida', 'cantidad', 'precio', 'subtotal', 'acciones'];
  readonly columnasMenuCotizacion = ['menu', 'bebida', 'cantidad', 'precio', 'subtotal'];
  readonly columnasServiciosCotizacion = ['tipo', 'descripcion', 'monto'];

  readonly formularioCosto = this.fb.nonNullable.group({
    idTipoCosto: this.fb.control<number | null>(null, Validators.required),
    descripcion: [''],
    monto: this.fb.control<number | null>(null, Validators.required),
    fechaCosto: this.fb.control<Date | null>(null, Validators.required),
    recargarCliente: false,
    idMetodoPago: this.fb.control<number | null>(null),
    referenciaTransaccion: [{ value: '', disabled: true }],
  });

  readonly costoEnEdicion = signal<CostoEventoResponse | null>(null);
  readonly personalEnEdicion = signal<EventoEmpleadoResponse | null>(null);
  readonly vehiculoEnEdicion = signal<EventoVehiculoResponse | null>(null);

  readonly formularioPersonal = this.fb.nonNullable.group({
    idEmpleado: this.fb.control<number | null>(null, Validators.required),
    salarioEvento: this.fb.control<number | null>(null, Validators.required),
  });

  readonly formularioVehiculo = this.fb.nonNullable.group({
    idVehiculo: this.fb.control<number | null>(null, Validators.required),
    idEmpleadoConductor: this.fb.control<number | null>(null),
  });

  readonly formularioInventario = this.fb.nonNullable.group({
    idProducto: this.fb.control<number | null>(null, Validators.required),
    cantidad: this.fb.control<number | null>(null, Validators.required),
  });

  readonly idProductoCorrigiendo = signal<number | null>(null);
  readonly controlCorreccion = this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]);

  readonly formularioMenu = this.fb.nonNullable.group({
    idMenu: this.fb.control<number | null>(null, Validators.required),
    idPlato: this.fb.control<number | null>(null, Validators.required),
    cantidadPlatos: [1, [Validators.required, Validators.min(1)]],
    observaciones: [''],
    idBebida: this.fb.control<number | null>(null),
  });
  readonly bebidasDelPlato = signal<BebidaResumen[]>([]);
  readonly esPorPersona = signal(true);

  constructor() {
    this.idEvento = Number(this.route.snapshot.paramMap.get('id'));
  }

  get puedeModificar(): boolean {
    return this.authService.tienePermiso(PAGINA_URL, 'modificacion') && this.evento()?.estadoNombre !== 'CANCELADO';
  }

  get puedeCancelar(): boolean {
    return this.authService.esAdministrador();
  }

  get puedeImprimirHoja(): boolean {
    const estado = this.evento()?.estadoNombre;
    return this.authService.tienePermiso(PAGINA_URL, 'imprimir') && !!estado && estado !== 'CANCELADO';
  }

  verHojaServicio(): void {
    const id = this.evento()?.idEvento;
    if (id) {
      this.verPdf.abrir(this.eventoService.hojaServicio(id), `Hoja de servicio — Evento #${id}`, `hoja-servicio-evento-${id}`);
    }
  }

  get puedeVerRentabilidad(): boolean {
    return this.authService.puedeVer('/api/rentabilidad');
  }

  get puedeEditarMenu(): boolean {
    const e = this.evento();
    return this.puedeModificar && !!e && e.idCotizacionVersion === null && this.esCreadoOPlanificado(e.estadoNombre);
  }

  get puedeAsignarRecursos(): boolean {
    return this.puedeModificar && this.esCreadoOPlanificado(this.evento()?.estadoNombre);
  }

  private esCreadoOPlanificado(estadoNombre: string | undefined): boolean {
    return estadoNombre === 'CREADO' || estadoNombre === 'PLANIFICADO';
  }

  get puedePlanificar(): boolean {
    return this.puedeModificar && this.evento()?.estadoNombre === 'CREADO';
  }

  get puedeCancelarEvento(): boolean {
    const estado = this.evento()?.estadoNombre;
    return this.puedeModificar && this.puedeCancelar && !!estado && estado !== 'FINALIZADO';
  }

  etiquetaAcuerdo(acuerdo: AcuerdoAnticipo): string {
    return ACUERDOS_ANTICIPO[acuerdo];
  }

  cancelarEvento(): void {
    this.eventoService.anticipo(this.idEvento).subscribe((anticipo) => {
      const ref = this.dialog.open<CancelarEventoDialog, CancelarEventoDialogData, CancelarEventoRequest>(
        CancelarEventoDialog,
        { data: { idEvento: this.idEvento, abonado: Number(anticipo.abonado) } },
      );
      ref.afterClosed().subscribe((request) => {
        if (!request) {
          return;
        }
        this.eventoService.cancelar(this.idEvento, request).subscribe(() => {
          this.snackBar.open('Evento cancelado', 'Cerrar', { duration: 3000 });
          this.cargar();
        });
      });
    });
  }

  colorEstado(estado: string): ColorEstado {
    return COLOR_POR_ESTADO_EVENTO[estado.toUpperCase()] ?? COLOR_ESTADO_EVENTO_DEFECTO;
  }

  metodoRequiereReferencia(idMetodoPago: number | null): boolean {
    return this.metodosPago().find((m) => m.idMetodoPago === idMetodoPago)?.requiereReferencia ?? false;
  }

  ngOnInit(): void {
    if (this.authService.puedeVer('/api/empleados')) {
      this.empleadoService.listar('', 0, 200).subscribe((p) => this.empleadosDisponibles.set(p.content));
    }
    if (this.authService.puedeVer('/api/vehiculos')) {
      this.vehiculoService.listar(0, 200).subscribe((p) => this.vehiculosDisponibles.set(p.content));
    }
    if (this.authService.puedeVer('/api/productos')) {
      this.productoService.listarTodos().subscribe((p) => this.productosDisponibles.set(p));
    }
    if (this.authService.puedeVer('/api/menus')) {
      this.menuService.listarActivos().subscribe((m) => this.menusDisponibles.set(m));
    }
    this.tipoCostoService.listar().subscribe((t) => this.tiposCosto.set(t));
    this.pagoService.listarMetodos().subscribe((m) => this.metodosPago.set(m));
    this.cargar();

    interval(20000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.eventoService.obtener(this.idEvento).subscribe((e) => this.evento.set(e)));

    this.formularioCosto.controls.recargarCliente.valueChanges.subscribe((recargar) => {
      const metodo = this.formularioCosto.controls.idMetodoPago;
      metodo.setValidators(recargar ? Validators.required : null);
      if (!recargar) {
        metodo.setValue(null);
        this.formularioCosto.controls.referenciaTransaccion.setValue('');
      }
      metodo.updateValueAndValidity();
    });

    this.formularioCosto.controls.idMetodoPago.valueChanges.subscribe((idMetodoPago) => {
      const referencia = this.formularioCosto.controls.referenciaTransaccion;
      const requiere = this.metodoRequiereReferencia(idMetodoPago);
      referencia.setValidators(requiere ? Validators.required : null);
      if (requiere) {
        referencia.enable();
      } else {
        referencia.setValue('');
        referencia.disable();
      }
      referencia.updateValueAndValidity();
    });

    this.formularioMenu.controls.idMenu.valueChanges.subscribe((idMenu) => {
      this.formularioMenu.controls.idPlato.setValue(null);
      this.precioSeleccionado.set(null);
      if (idMenu == null) {
        this.platosDelMenu.set([]);
        return;
      }
      this.menuService.listarPlatosDeMenu(idMenu).subscribe((platos) => this.platosDelMenu.set(platos));
    });

    this.formularioMenu.controls.idPlato.valueChanges.subscribe((idPlato) => {
      const plato = this.platosDelMenu().find((p) => p.idPlato === idPlato);
      this.precioSeleccionado.set(plato ? precioPorUnidad(plato, this.evento()?.cantidadPersonas ?? 0) : null);
      this.esPorPersona.set((plato?.unidadVenta ?? 'PERSONA') === 'PERSONA');
      const bebidas = plato?.bebidas ?? [];
      this.bebidasDelPlato.set(bebidas);
      this.formularioMenu.controls.idBebida.setValue(bebidas.length === 1 ? bebidas[0].idBebida : null);
    });
  }

  etiquetaPrecio(plato: MenuPlatoResponse): string {
    const volumen = (this.evento()?.cantidadPersonas ?? 0) >= PERSONAS_PRECIO_VOLUMEN;
    const precio = plato.precioDesde100 != null && volumen ? plato.precioDesde100 : plato.precioUnitario;
    return `Q${Number(precio).toFixed(2)} ${PRECIO_POR[plato.unidadVenta]}`;
  }

  get puedeVerMontosDelEvento(): boolean {
    return this.authService.tienePermiso(PAGINA_URL, 'alta') || this.authService.puedeVer('/api/rentabilidad');
  }

  cargar(): void {
    this.eventoService.obtener(this.idEvento).subscribe((e) => {
      this.evento.set(e);
      if (e.idCotizacionVersion !== null && this.authService.puedeVer('/api/cotizaciones')) {
        this.cotizacionService.listarDetalle(e.idCotizacionVersion).subscribe((d) => this.detalleCotizacionOrigen.set(d));
        this.cotizacionService.listarServicios(e.idCotizacionVersion).subscribe((s) => this.serviciosCotizacionOrigen.set(s));
      }
    });
    if (this.puedeVerMontosDelEvento) {
      this.eventoService.listarCostos(this.idEvento).subscribe((c) => this.costos.set(c));
      this.eventoService.listarPersonal(this.idEvento).subscribe((p) => this.personal.set(p));
    }
    this.eventoService.listarVehiculos(this.idEvento).subscribe((v) => this.vehiculos.set(v));
    this.eventoService.listarInventario(this.idEvento).subscribe((i) => this.inventario.set(i));
    this.eventoService.listarDetalle(this.idEvento).subscribe((d) => this.detalleMenu.set(d));
  }

  private seccionesFaltantesParaPlanificar(): string[] {
    const faltantes: string[] = [];
    if (this.lineasMenuDetalle.length === 0) {
      faltantes.push('Menú');
    }
    if (this.personal().length === 0) {
      faltantes.push('Personal');
    }
    if (this.vehiculos().length === 0) {
      faltantes.push('Vehículos');
    }
    if (this.inventario().length === 0) {
      faltantes.push('Inventario');
    }
    return faltantes;
  }

  planificarEvento(): void {
    const faltantes = this.seccionesFaltantesParaPlanificar();
    if (faltantes.length > 0) {
      this.snackBar.open(`Para planificar primero hay que completar: ${faltantes.join(', ')}`, 'Cerrar', {
        duration: 5000,
      });
      return;
    }
    this.eventoService.anticipo(this.idEvento).subscribe((anticipo) => {
      const q = (n: number) => `Q${Number(n).toLocaleString('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
      const mensaje = anticipo.cubierto
        ? '¿Confirmar que el evento ya está listo (menú, personal, vehículos e inventario)?'
        : `El cliente ha pagado ${q(anticipo.abonado)} de ${q(anticipo.total)}; falta ${q(anticipo.faltante)} para el 50%` +
          ' que se cobra una semana antes del evento.\n\n¿Planificarlo de todos modos? Quedará registrado.';
      const ref = this.dialog.open(ConfirmDialog, {
        data: { titulo: anticipo.cubierto ? 'Planificar evento' : 'Falta el primer 50%', mensaje },
      });
      ref.afterClosed().subscribe((confirmado) => {
        if (!confirmado) {
          return;
        }
        this.eventoService.planificar(this.idEvento, !anticipo.cubierto).subscribe(() => {
          this.snackBar.open('Evento planificado', 'Cerrar', { duration: 3000 });
          this.cargar();
        });
      });
    });
  }

  editarCosto(costo: CostoEventoResponse): void {
    this.costoEnEdicion.set(costo);
    this.formularioCosto.reset();
    this.formularioCosto.patchValue({
      idTipoCosto: costo.idTipoCosto,
      descripcion: costo.descripcion ?? '',
      monto: costo.monto,
      fechaCosto: new Date(`${costo.fechaCosto}T00:00:00`),
    });
    if (costo.pagado) {
      this.formularioCosto.controls.monto.disable();
    }
  }

  cancelarEdicionCosto(): void {
    this.costoEnEdicion.set(null);
    this.formularioCosto.controls.monto.enable();
    this.formularioCosto.reset();
  }

  guardarCosto(): void {
    const enEdicion = this.costoEnEdicion();
    if (!enEdicion) {
      this.agregarCosto();
      return;
    }
    if (this.formularioCosto.invalid) {
      this.formularioCosto.markAllAsTouched();
      return;
    }
    const v = this.formularioCosto.getRawValue();
    this.eventoService
      .actualizarCosto(this.idEvento, enEdicion.idCostoEvento, {
        idTipoCosto: v.idTipoCosto!,
        descripcion: v.descripcion || null,
        monto: v.monto!,
        fechaCosto: this.aFechaIso(v.fechaCosto),
      })
      .subscribe(() => {
        this.cancelarEdicionCosto();
        this.snackBar.open('Costo actualizado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
  }

  agregarCosto(): void {
    if (this.formularioCosto.invalid) {
      this.formularioCosto.markAllAsTouched();
      return;
    }
    const v = this.formularioCosto.getRawValue();
    this.eventoService
      .agregarCosto(this.idEvento, {
        idTipoCosto: v.idTipoCosto!,
        descripcion: v.descripcion || null,
        monto: v.monto!,
        fechaCosto: this.aFechaIso(v.fechaCosto),
      })
      .pipe(
        switchMap((costoCreado) =>
          v.recargarCliente
            ? this.crearPagoDeRecargo(costoCreado.idCostoEvento, v.monto!, v.idMetodoPago!, v.referenciaTransaccion,
                v.descripcion, this.aFechaIso(v.fechaCosto)!)
            : of(null),
        ),
      )
      .subscribe(() => {
        this.formularioCosto.reset();
        this.snackBar.open(
          v.recargarCliente ? 'Costo y pago del cliente registrados' : 'Costo agregado',
          'Cerrar',
          { duration: 3000 },
        );
        this.cargar();
      });
  }

  private crearPagoDeRecargo(
    idCostoEvento: number,
    monto: number,
    idMetodoPago: number,
    referencia: string,
    descripcionCosto: string,
    fecha: string,
  ) {
    return this.pagoService.crear({
      idEvento: this.idEvento,
      idMetodoPago,
      monto,
      referenciaTransaccion: referencia || null,
      observaciones: descripcionCosto ? `Recargo por: ${descripcionCosto}` : 'Recargo por costo extra del evento',
      fechaPago: `${fecha}T00:00:00`,
      idCostoEvento,
    });
  }

  eliminarCosto(costo: CostoEventoResponse): void {
    this.eventoService.eliminarCosto(this.idEvento, costo.idCostoEvento).subscribe(() => this.cargar());
  }

  asignarEmpleado(): void {
    if (this.formularioPersonal.invalid) {
      this.formularioPersonal.markAllAsTouched();
      return;
    }
    const v = this.formularioPersonal.getRawValue();
    const evento = this.evento();
    this.eventoService
      .asignarEmpleado(this.idEvento, v.idEmpleado!, {
        salarioEvento: v.salarioEvento!,
        horaInicio: evento?.horaInicio ?? null,
        horaFin: evento?.horaFin ?? null,
        idEstado: null,
      })
      .subscribe(() => {
        this.formularioPersonal.reset();
        this.cargar();
      });
  }

  editarEmpleado(asignacion: EventoEmpleadoResponse): void {
    this.personalEnEdicion.set(asignacion);
    this.formularioPersonal.reset();
    this.formularioPersonal.patchValue({ idEmpleado: asignacion.idEmpleado, salarioEvento: asignacion.salarioEvento });
  }

  cancelarEdicionEmpleado(): void {
    this.personalEnEdicion.set(null);
    this.formularioPersonal.reset();
  }

  guardarEmpleado(): void {
    const enEdicion = this.personalEnEdicion();
    if (!enEdicion) {
      this.asignarEmpleado();
      return;
    }
    if (this.formularioPersonal.invalid) {
      this.formularioPersonal.markAllAsTouched();
      return;
    }
    this.eventoService
      .actualizarEmpleado(this.idEvento, enEdicion.idEmpleado, {
        salarioEvento: this.formularioPersonal.getRawValue().salarioEvento!,
        horaInicio: enEdicion.horaInicio,
        horaFin: enEdicion.horaFin,
        idEstado: enEdicion.idEstado,
      })
      .subscribe(() => {
        this.cancelarEdicionEmpleado();
        this.snackBar.open('Pago del empleado actualizado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
  }

  quitarEmpleado(asignacion: EventoEmpleadoResponse): void {
    this.eventoService.quitarEmpleado(this.idEvento, asignacion.idEmpleado).subscribe(() => this.cargar());
  }

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

  editarVehiculo(asignacion: EventoVehiculoResponse): void {
    this.vehiculoEnEdicion.set(asignacion);
    this.formularioVehiculo.reset();
    this.formularioVehiculo.patchValue({
      idVehiculo: asignacion.idVehiculo,
      idEmpleadoConductor: asignacion.idEmpleadoConductor,
    });
  }

  cancelarEdicionVehiculo(): void {
    this.vehiculoEnEdicion.set(null);
    this.formularioVehiculo.reset();
  }

  guardarVehiculo(): void {
    const enEdicion = this.vehiculoEnEdicion();
    if (!enEdicion) {
      this.asignarVehiculo();
      return;
    }
    this.eventoService
      .actualizarVehiculo(this.idEvento, enEdicion.idVehiculo, {
        idEmpleadoConductor: this.formularioVehiculo.getRawValue().idEmpleadoConductor,
      })
      .subscribe(() => {
        this.cancelarEdicionVehiculo();
        this.snackBar.open('Conductor actualizado', 'Cerrar', { duration: 3000 });
        this.cargar();
      });
  }

  quitarVehiculo(asignacion: EventoVehiculoResponse): void {
    this.eventoService.quitarVehiculo(this.idEvento, asignacion.idVehiculo).subscribe(() => this.cargar());
  }

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

  get hayInventarioPendiente(): boolean {
    return this.inventario().some((i) => !i.fechaConsumo);
  }

  get columnasInventario(): string[] {
    const hayAccionPosible = this.inventario().some((i) =>
      i.fechaConsumo ? this.puedeModificar : this.puedeAsignarRecursos,
    );
    return hayAccionPosible ? [...this.columnasInventarioBase, 'acciones'] : this.columnasInventarioBase;
  }

  get lineasMenuDetalle(): (DetalleEventoResponse | DetalleCotizacionResponse)[] {
    return this.evento()?.idCotizacionVersion != null ? this.detalleCotizacionOrigen() : this.detalleMenu();
  }

  get subtotalMenuDetalle(): number {
    return this.lineasMenuDetalle.reduce((acc, d) => acc + d.subtotal, 0);
  }

  get subtotalServiciosDetalle(): number {
    return this.serviciosCotizacionOrigen().reduce((acc, s) => acc + s.monto, 0);
  }

  get subtotalCostosDetalle(): number {
    return this.costos().reduce((acc, c) => acc + c.monto, 0);
  }

  get subtotalPersonalDetalle(): number {
    return this.personal().reduce((acc, p) => acc + p.salarioEvento, 0);
  }

  precioUnitarioProducto(idProducto: number): number {
    return this.productosDisponibles().find((p) => p.idProducto === idProducto)?.precioUnitario ?? 0;
  }

  costoInventarioLinea(item: EventoInventarioResponse): number {
    return item.fechaConsumo ? item.cantidad * this.precioUnitarioProducto(item.idProducto) : 0;
  }

  get subtotalInventarioDetalle(): number {
    return this.inventario().reduce((acc, i) => acc + this.costoInventarioLinea(i), 0);
  }

  get totalGastosDetalle(): number {
    return this.subtotalCostosDetalle + this.subtotalPersonalDetalle + this.subtotalInventarioDetalle;
  }

  contadorTexto(cantidad: number, singular: string, plural: string): string {
    return `${cantidad} ${cantidad === 1 ? singular : plural}`;
  }

  iniciarCorreccionInventario(item: EventoInventarioResponse): void {
    this.idProductoCorrigiendo.set(item.idProducto);
    this.controlCorreccion.setValue(item.cantidad);
  }

  cancelarCorreccionInventario(): void {
    this.idProductoCorrigiendo.set(null);
  }

  guardarCorreccionInventario(item: EventoInventarioResponse): void {
    if (this.controlCorreccion.invalid) {
      this.controlCorreccion.markAsTouched();
      return;
    }
    const cantidadCorrecta = this.controlCorreccion.value!;
    const ref = this.dialog.open(ConfirmDialog, {
      data: {
        titulo: 'Corregir consumo confirmado',
        mensaje: `¿Corregir "${item.nombreProducto}" de ${item.cantidad} a ${cantidadCorrecta}?, esto ajustará el stock en el inventario`,
      },
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.eventoService.corregirConsumo(this.idEvento, item.idProducto, { cantidadCorrecta }).subscribe(() => {
        this.snackBar.open('Consumo corregido, stock ajustado', 'Cerrar', { duration: 3000 });
        this.idProductoCorrigiendo.set(null);
        this.cargar();
      });
    });
  }

  confirmarTodoInventario(): void {
    const pendientes = this.inventario().filter((i) => !i.fechaConsumo).length;
    const ref = this.dialog.open(ConfirmDialog, {
      data: {
        titulo: 'Confirmar todo el consumo',
        mensaje: `¿Confirmar el consumo de los ${pendientes} productos registrados?, Esto se descontará del stock disponible.`,
      },
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.eventoService.confirmarTodoInventario(this.idEvento).subscribe((resultado) => {
        const mensaje = resultado.fallidos.length
          ? `${resultado.confirmados.length} confirmados. ${resultado.fallidos.length} sin stock suficiente: ` +
            resultado.fallidos.map((f) => f.nombreProducto).join(', ')
          : `${resultado.confirmados.length} productos confirmados, stock actualizado`;
        this.snackBar.open(mensaje, 'Cerrar', { duration: resultado.fallidos.length ? 8000 : 3000 });
        this.cargar();
      });
    });
  }

  agregarLineaMenu(): void {
    if (this.formularioMenu.invalid) {
      this.formularioMenu.markAllAsTouched();
      return;
    }
    const v = this.formularioMenu.getRawValue();
    this.eventoService
      .agregarDetalle(this.idEvento, {
        idMenu: v.idMenu!,
        idPlato: v.idPlato!,
        cantidadPlatos: v.cantidadPlatos,
        observaciones: v.observaciones || null,
        idBebida: v.idBebida,
      })
      .subscribe(() => {
        this.platosDelMenu.set([]);
        this.precioSeleccionado.set(null);
        this.bebidasDelPlato.set([]);
        this.formularioMenu.reset({ idMenu: null, idPlato: null, cantidadPlatos: 1, observaciones: '', idBebida: null });
        this.cargar();
      });
  }

  eliminarLineaMenu(detalle: DetalleEventoResponse): void {
    this.eventoService.eliminarDetalle(this.idEvento, detalle.idDetalleEvento).subscribe(() => {
      this.snackBar.open('Linea eliminada', 'Cerrar', { duration: 3000 });
      this.cargar();
    });
  }

  private aFechaIso(fecha: Date | null): string | null {
    if (!fecha) {
      return null;
    }
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }
}
