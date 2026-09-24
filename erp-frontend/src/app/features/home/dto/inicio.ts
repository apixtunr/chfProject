/**
 * Respuesta de GET /api/inicio. Cada seccion llega en null cuando el rol del usuario no
 * tiene acceso a la pantalla de donde salen esos datos: null = "no aplica", lista vacia =
 * "aplica, pero no hay nada pendiente".
 */
export interface PanelInicio {
  fechaReferencia: string;
  rol: string;
  resumen: ResumenInicio;
  porPreparar: EventoPanel[] | null;
  agenda: EventoPanel[] | null;
  cotizacionesEnviadas: CotizacionPanel[] | null;
  cotizacionesAceptadasSinEvento: CotizacionPanel[] | null;
  bajoStock: StockPanel[] | null;
  insumosFaltantes: InsumoPanel[] | null;
  cobrosPendientes: CobroPanel[] | null;
  actividad: ActividadPanel | null;
}

export interface ResumenInicio {
  eventosSemana: number | null;
  eventosPorPreparar: number | null;
  cotizacionesEnviadas: number | null;
  productosBajoStock: number | null;
  saldoPendiente: number | null;
  eventosConSaldo: number | null;
}

export interface EventoPanel {
  idEvento: number;
  fechaEvento: string;
  horaInicio: string | null;
  horaFin: string | null;
  clienteNombre: string | null;
  tipoEventoNombre: string;
  direccionUbicacion: string;
  cantidadPersonas: number | null;
  estadoNombre: string;
  requiereMenu: boolean;
  tieneMenu: boolean;
  tienePersonal: boolean;
  tieneVehiculos: boolean;
  tieneInventario: boolean;
  faltantes: string[];
  menus: string[];
}

export interface CotizacionPanel {
  idCotizacion: number;
  idCotizacionVersion: number;
  numeroVersion: number;
  clienteNombre: string;
  tipoEventoNombre: string | null;
  fechaEvento: string | null;
  montoTotal: number | null;
  fechaVersion: string | null;
}

export interface StockPanel {
  idProducto: number;
  nombreProducto: string;
  unidadMedida: string | null;
  cantidadActual: number;
  cantidadMinima: number;
}

export interface InsumoPanel {
  idProducto: number;
  nombreProducto: string;
  unidadMedida: string | null;
  requerido: number;
  disponible: number;
  faltante: number;
  primerEvento: string;
  cantidadEventos: number;
}

export interface CobroPanel {
  idEvento: number;
  fechaEvento: string;
  clienteNombre: string | null;
  tipoEventoNombre: string;
  estadoNombre: string;
  total: number;
  abonado: number;
  pendiente: number;
}

export interface ActividadPanel {
  ingresosHoy: number;
  ingresosFallidosHoy: number;
  cambiosHoy: number;
  recientes: MovimientoReciente[];
}

export interface MovimientoReciente {
  usuario: string;
  tabla: string;
  registroId: string;
  operacion: 'INSERT' | 'UPDATE' | 'DELETE' | string;
  fecha: string;
}
