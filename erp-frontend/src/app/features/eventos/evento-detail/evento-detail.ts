import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
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
import { EstadoResponse } from '../../../core/catalogos/estado';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { MenuPlatoResponse, MenuResponse } from '../../../core/catalogos/menu';
import { MenuService } from '../../../core/catalogos/menu.service';
import { ProductoResponse } from '../../../core/catalogos/producto';
import { ProductoService } from '../../../core/catalogos/producto.service';
import { TipoCostoResponse } from '../../../core/catalogos/tipo-costo';
import { TipoCostoService } from '../../../core/catalogos/tipo-costo.service';
import { ConfirmDialog } from '../../../shared/confirm-dialog/confirm-dialog';
import { EmpleadoResponse } from '../../empleados/dto/empleado';
import { EmpleadoService } from '../../empleados/empleado.service';
import { VehiculoResponse } from '../../vehiculos/dto/vehiculo';
import { VehiculoService } from '../../vehiculos/vehiculo.service';
import { MetodoPagoResponse } from '../../pagos/dto/pago';
import { PagoService } from '../../pagos/pago.service';
import { CotizacionService } from '../../cotizaciones/cotizacion.service';
import { DetalleCotizacionResponse, ServicioCotizacionResponse } from '../../cotizaciones/dto/cotizacion';
import {
  ColorEstado,
  COLOR_ESTADO_EVENTO_DEFECTO,
  COLOR_POR_ESTADO_EVENTO,
  CostoEventoResponse,
  DetalleEventoResponse,
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
  readonly detalleMenu = signal<DetalleEventoResponse[]>([]);
  /** Cuando el evento nacio de una cotizacion, su menu es el que ahi se acepto (solo lectura). */
  readonly detalleCotizacionOrigen = signal<DetalleCotizacionResponse[]>([]);
  readonly serviciosCotizacionOrigen = signal<ServicioCotizacionResponse[]>([]);

  readonly empleadosDisponibles = signal<EmpleadoResponse[]>([]);
  readonly vehiculosDisponibles = signal<VehiculoResponse[]>([]);
  readonly productosDisponibles = signal<ProductoResponse[]>([]);
  readonly tiposCosto = signal<TipoCostoResponse[]>([]);
  readonly estadosEvento = signal<EstadoResponse[]>([]);
  readonly metodosPago = signal<MetodoPagoResponse[]>([]);
  readonly menusDisponibles = signal<MenuResponse[]>([]);
  readonly platosDelMenu = signal<MenuPlatoResponse[]>([]);
  readonly precioSeleccionado = signal<number | null>(null);

  readonly columnasCostos = ['tipo', 'descripcion', 'monto', 'fecha', 'pagado', 'acciones'];
  readonly columnasPersonal = ['empleado', 'salario', 'horario', 'acciones'];
  readonly columnasVehiculos = ['placa', 'conductor', 'acciones'];
  private readonly columnasInventarioBase = ['producto', 'cantidad', 'consumo', 'fechaConfirmacion'];
  readonly columnasMenu = ['menu', 'cantidad', 'precio', 'subtotal', 'acciones'];
  readonly columnasMenuCotizacion = ['menu', 'cantidad', 'precio', 'subtotal'];
  readonly columnasServiciosCotizacion = ['tipo', 'descripcion', 'monto'];

  readonly formularioCosto = this.fb.nonNullable.group({
    idTipoCosto: this.fb.control<number | null>(null, Validators.required),
    descripcion: [''],
    monto: this.fb.control<number | null>(null, Validators.required),
    /** Si se deja vacio, el backend usa la fecha de hoy. */
    fechaCosto: this.fb.control<Date | null>(null),
    /** Si el cliente cubre este costo, se crea de una vez el pago correspondiente. */
    recargarCliente: false,
    idMetodoPago: this.fb.control<number | null>(null),
    referenciaTransaccion: [''],
  });

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

  /** Producto cuya fila de inventario ya confirmada se esta corrigiendo (null = ninguna). */
  readonly idProductoCorrigiendo = signal<number | null>(null);
  readonly controlCorreccion = this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]);

  readonly formularioMenu = this.fb.nonNullable.group({
    idMenu: this.fb.control<number | null>(null, Validators.required),
    idPlato: this.fb.control<number | null>(null, Validators.required),
    cantidadPlatos: [1, [Validators.required, Validators.min(1)]],
    observaciones: [''],
  });

  constructor() {
    this.idEvento = Number(this.route.snapshot.paramMap.get('id'));
  }

  /** Un evento CANCELADO queda de solo lectura: ya no se le agrega ni quita nada. */
  get puedeModificar(): boolean {
    return this.authService.tienePermiso(PAGINA_URL, 'modificacion') && this.evento()?.estadoNombre !== 'CANCELADO';
  }

  get puedeCancelar(): boolean {
    return this.authService.esAdministrador();
  }

  get puedeVerRentabilidad(): boolean {
    return this.authService.puedeVer('/api/rentabilidad');
  }

  /** El menu solo se puede armar aqui cuando el evento es directo (sin cotizacion) y todavia
   * se esta armando (CREADO o PLANIFICADO). */
  get puedeEditarMenu(): boolean {
    const e = this.evento();
    return this.puedeModificar && !!e && e.idCotizacionVersion === null && this.esCreadoOPlanificado(e.estadoNombre);
  }

  /**
   * Personal, vehiculos e inventario se definen mientras el evento se sigue armando (CREADO
   * o ya PLANIFICADO); una vez que inicio (EN CURSO) o termino (FINALIZADO), ya no tiene
   * sentido seguir agregando o quitando. Lo unico que se puede seguir ingresando en cualquier
   * estado no cancelado es un costo extra.
   */
  get puedeAsignarRecursos(): boolean {
    return this.puedeModificar && this.esCreadoOPlanificado(this.evento()?.estadoNombre);
  }

  private esCreadoOPlanificado(estadoNombre: string | undefined): boolean {
    return estadoNombre === 'CREADO' || estadoNombre === 'PLANIFICADO';
  }

  /** El boton "Planificar" solo aplica mientras el evento sigue en CREADO. */
  get puedePlanificar(): boolean {
    return this.puedeModificar && this.evento()?.estadoNombre === 'CREADO';
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

  colorEstado(estado: string): ColorEstado {
    return COLOR_POR_ESTADO_EVENTO[estado.toUpperCase()] ?? COLOR_ESTADO_EVENTO_DEFECTO;
  }

  metodoRequiereReferencia(idMetodoPago: number | null): boolean {
    return this.metodosPago().find((m) => m.idMetodoPago === idMetodoPago)?.requiereReferencia ?? false;
  }

  ngOnInit(): void {
    // Las listas para elegir (personal, vehiculos, productos, menus) solo se piden si el
    // rol puede leer ese modulo. Antes se pedian siempre: con el control de acceso del
    // backend en su lugar, un usuario de Cocina abria el detalle y disparaba siete
    // rechazos, cada uno con su aviso de error en pantalla, sin haber hecho nada malo.
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
    // Estos tres son catalogos: cualquiera con sesion los puede leer.
    this.tipoCostoService.listar().subscribe((t) => this.tiposCosto.set(t));
    this.estadoService.listarPorTipo(TIPO_ESTADO_EVENTO).subscribe((e) => this.estadosEvento.set(e));
    this.pagoService.listarMetodos().subscribe((m) => this.metodosPago.set(m));
    this.cargar();

    // El backend cambia EN CURSO/FINALIZADO solo, en el instante exacto de la hora
    // cargada; sin esto, la pantalla se queda mostrando el estado viejo hasta que el
    // usuario haga algo que recargue los datos (F5, cambiar de tab, etc.).
    interval(20000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.eventoService.obtener(this.idEvento).subscribe((e) => this.evento.set(e)));

    // Si el cliente cubre el costo, hay que elegir metodo de pago; y si ese
    // metodo exige referencia (tarjeta, transferencia), tambien se vuelve obligatoria.
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
        // Efectivo, por ejemplo, no tiene numero de referencia: se bloquea y se limpia.
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
      this.precioSeleccionado.set(plato?.precioUnitario ?? null);
    });
  }

  /**
   * Si este usuario puede ver los montos del evento: salarios del personal asignado y
   * costos. Espeja la regla del backend (RecursosApi.MODULO_FINANCIERO): los ve quien
   * carga los eventos, porque es quien fija esos montos, o quien lleva las finanzas.
   *
   * Ver la agenda no alcanza: Cocina y Bodega la necesitan para trabajar, pero no tienen
   * por que saber cuanto gana cada companero.
   */
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

  /** Chequeo rapido en el cliente (el backend igual lo valida de verdad): evita el viaje
   * al servidor para el caso comun de que a todos se les olvida algo antes de planificar. */
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
    const ref = this.dialog.open(ConfirmDialog, {
      data: {
        titulo: 'Planificar evento',
        mensaje: '¿Confirmar que el evento ya está listo (menú, personal, vehículos e inventario)?',
      },
    });
    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) {
        return;
      }
      this.eventoService.planificar(this.idEvento).subscribe(() => {
        this.snackBar.open('Evento planificado', 'Cerrar', { duration: 3000 });
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
      .agregarCosto(this.idEvento, {
        idTipoCosto: v.idTipoCosto!,
        descripcion: v.descripcion || null,
        monto: v.monto!,
        fechaCosto: this.aFechaIso(v.fechaCosto),
      })
      .pipe(
        switchMap((costoCreado) =>
          v.recargarCliente
            ? this.crearPagoDeRecargo(costoCreado.idCostoEvento, v.monto!, v.idMetodoPago!, v.referenciaTransaccion, v.descripcion)
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

  /** Crea el pago del recargo (nace CONFIRMADO de una vez, registrar el pago ya es confirmarlo). */
  private crearPagoDeRecargo(
    idCostoEvento: number,
    monto: number,
    idMetodoPago: number,
    referencia: string,
    descripcionCosto: string,
  ) {
    return this.pagoService.crear({
      idEvento: this.idEvento,
      idMetodoPago,
      monto,
      referenciaTransaccion: referencia || null,
      observaciones: descripcionCosto ? `Recargo por: ${descripcionCosto}` : 'Recargo por costo extra del evento',
      fechaPago: null,
      idCostoEvento,
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
    // El personal se asigna por la duracion completa del evento, no se pide hora
    // aparte: si alguien se queda mas tiempo del previsto, eso se registra como
    // costo extra (Personal extra), no como un horario distinto aqui.
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

  get hayInventarioPendiente(): boolean {
    return this.inventario().some((i) => !i.fechaConsumo);
  }

  /** La columna de acciones se muestra si alguna fila tiene algo que hacer: eliminar (pendiente,
   * solo mientras se puede asignar recursos) o corregir (ya confirmado, en cualquier estado no
   * cancelado). Si ninguna fila califica, la columna no aporta nada y se oculta. */
  get columnasInventario(): string[] {
    const hayAccionPosible = this.inventario().some((i) =>
      i.fechaConsumo ? this.puedeModificar : this.puedeAsignarRecursos,
    );
    return hayAccionPosible ? [...this.columnasInventarioBase, 'acciones'] : this.columnasInventarioBase;
  }

  // --- Detalle: solo listas y sumas simples de lo que ya esta cargado en cada pestana.
  // La ganancia real (ingresos por Pagos menos costos) vive en Rentabilidad, no aqui. ---

  /** El menu viene de la cotizacion de origen si el evento nacio de una, si no del propio evento. */
  get lineasMenuDetalle(): (DetalleEventoResponse | DetalleCotizacionResponse)[] {
    return this.evento()?.idCotizacionVersion != null ? this.detalleCotizacionOrigen() : this.detalleMenu();
  }

  get subtotalMenuDetalle(): number {
    return this.lineasMenuDetalle.reduce((acc, d) => acc + d.subtotal, 0);
  }

  /** Solo existe si el evento nacio de una cotizacion; un evento directo nunca tiene. */
  get subtotalServiciosDetalle(): number {
    return this.serviciosCotizacionOrigen().reduce((acc, s) => acc + s.monto, 0);
  }

  get subtotalCostosDetalle(): number {
    return this.costos().reduce((acc, c) => acc + c.monto, 0);
  }

  get subtotalPersonalDetalle(): number {
    return this.personal().reduce((acc, p) => acc + p.salarioEvento, 0);
  }

  /** precio_unitario del producto no viaja en EventoInventarioResponse; se busca en el
   * catalogo ya cargado (productosDisponibles) por idProducto. */
  precioUnitarioProducto(idProducto: number): number {
    return this.productosDisponibles().find((p) => p.idProducto === idProducto)?.precioUnitario ?? 0;
  }

  /** Solo lo ya Confirmado representa un gasto real; lo Planificado todavia no se uso. */
  costoInventarioLinea(item: EventoInventarioResponse): number {
    return item.fechaConsumo ? item.cantidad * this.precioUnitarioProducto(item.idProducto) : 0;
  }

  get subtotalInventarioDetalle(): number {
    return this.inventario().reduce((acc, i) => acc + this.costoInventarioLinea(i), 0);
  }

  /** Gasto operativo simple (no es la ganancia: le falta restar contra los ingresos reales,
   * eso lo hace Rentabilidad con los Pagos). Vehiculos no entra, no tiene costo registrado. */
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

  // --- Menu (solo eventos directos, sin cotizacion) ---

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
      })
      .subscribe(() => {
        this.platosDelMenu.set([]);
        this.precioSeleccionado.set(null);
        this.formularioMenu.reset({ idMenu: null, idPlato: null, cantidadPlatos: 1, observaciones: '' });
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
