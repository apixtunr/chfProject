/** Espeja evento/dto/*.java del backend. */
export interface EventoRequest {
  idCotizacionVersion: number | null;
  idCliente: number | null;
  idTipoEvento: number;
  idUbicacion: number;
  fechaEvento: string;
  horaInicio: string | null;
  horaFin: string | null;
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

export interface DetalleEventoRequest {
  idMenu: number;
  cantidadPlatos: number;
  precioUnitario: number;
  observaciones: string | null;
}

export interface DetalleEventoResponse {
  idDetalleEvento: number;
  idEvento: number;
  idMenu: number;
  nombreMenu: string;
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
