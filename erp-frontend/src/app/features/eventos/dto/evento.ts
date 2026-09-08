/** Espeja evento/dto/*.java del backend. */
export interface EventoRequest {
  idCotizacionVersion: number | null;
  /** Obligatorio solo si idCotizacionVersion viene vacio (evento directo). */
  idCliente: number | null;
  /** Obligatorio solo si idCotizacionVersion viene vacio; si no, se toma de la cotizacion. */
  idTipoEvento: number | null;
  /** Obligatorio solo si idCotizacionVersion viene vacio; si no, se toma de la cotizacion. */
  idUbicacion: number | null;
  /** Obligatoria solo si idCotizacionVersion viene vacio; si no, se toma de la cotizacion. */
  fechaEvento: string | null;
  horaInicio: string | null;
  horaFin: string | null;
  /** Obligatoria solo si idCotizacionVersion viene vacio; si no, se toma de la cotizacion. */
  cantidadPersonas: number | null;
  observaciones: string | null;
}

export interface EventoResponse {
  idEvento: number;
  idCotizacionVersion: number | null;
  idCliente: number;
  clienteNombre: string;
  idTipoEvento: number;
  tipoEventoNombre: string;
  idUbicacion: number;
  direccionUbicacion: string;
  idEstado: number;
  estadoNombre: string;
  fechaEvento: string;
  horaInicio: string | null;
  horaFin: string | null;
  cantidadPersonas: number | null;
  observaciones: string | null;
  montoMenu: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface ConteoResponse {
  etiqueta: string;
  cantidad: number;
}

export interface EventoResumenResponse {
  totalEventos: number;
  porEstado: ConteoResponse[];
  porTipo: ConteoResponse[];
}

export interface FiltrosEvento {
  fechaDesde: string | null;
  fechaHasta: string | null;
  idCliente: number | null;
  idTipoEvento: number | null;
  idEstado: number | null;
}

export interface DetalleEventoRequest {
  idMenu: number;
  idPlato: number;
  cantidadPlatos: number;
  observaciones: string | null;
}

export interface DetalleEventoResponse {
  idDetalleEvento: number;
  idEvento: number;
  idMenu: number;
  nombreMenu: string;
  idPlato: number;
  nombrePlato: string;
  cantidadPlatos: number;
  precioUnitario: number;
  subtotal: number;
  observaciones: string | null;
  montoMenuEvento: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface CostoEventoRequest {
  idTipoCosto: number;
  descripcion: string | null;
  monto: number;
  fechaCosto: string | null;
}

export interface CostoEventoResponse {
  idCostoEvento: number;
  idEvento: number;
  idTipoCosto: number;
  tipoCostoNombre: string;
  descripcion: string | null;
  monto: number;
  fechaCosto: string;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface EventoEmpleadoRequest {
  salarioEvento: number;
  horaInicio: string | null;
  horaFin: string | null;
  idEstado: number | null;
}

export interface EventoEmpleadoResponse {
  idEvento: number;
  idEmpleado: number;
  nombreEmpleado: string;
  idEstado: number;
  estadoNombre: string;
  salarioEvento: number;
  fechaAsignacion: string;
  horaInicio: string | null;
  horaFin: string | null;
}

export interface EventoVehiculoRequest {
  idEmpleadoConductor: number | null;
}

export interface EventoVehiculoResponse {
  idEvento: number;
  idVehiculo: number;
  placaVehiculo: string;
  idEmpleadoConductor: number | null;
  nombreConductor: string | null;
  fechaAsignacion: string;
}

export interface EventoInventarioRequest {
  cantidad: number;
  fechaConsumo: string | null;
}

export interface EventoInventarioResponse {
  idEvento: number;
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  fechaConsumo: string | null;
}

export interface TipoEventoResponse {
  idTipoEvento: number;
  nombreTipo: string;
}

/** Transiciones validas por estado (espeja EventoServiceImpl.TRANSICIONES_VALIDAS). */
export const TRANSICIONES_VALIDAS_EVENTO: Record<string, string[]> = {
  PLANIFICADO: ['EN CURSO', 'CANCELADO'],
  'EN CURSO': ['FINALIZADO', 'CANCELADO'],
};

export interface ColorEstado {
  bg: string;
  text: string;
}

/** Colores fijos por estado de evento (fondo suave + texto), compartidos entre los chips y la grafica de Reporte de eventos. */
export const COLOR_POR_ESTADO_EVENTO: Record<string, ColorEstado> = {
  FINALIZADO: { bg: '#ECFDF5', text: '#047857' },
  CANCELADO: { bg: '#FFF1F2', text: '#BE123C' },
  PLANIFICADO: { bg: '#EFF6FF', text: '#1D4ED8' },
  'EN CURSO': { bg: '#FFFBEB', text: '#92400E' },
};
export const COLOR_ESTADO_EVENTO_DEFECTO: ColorEstado = { bg: '#F3F4F6', text: '#374151' };
