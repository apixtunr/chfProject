/** Espeja cotizacion/dto/*.java del backend. */
export interface CotizacionRequest {
  idCliente: number;
  idTipoEvento: number;
  idUbicacion: number;
  cantidadPersonas: number;
  fechaEvento: string | null;
  presupuestoCliente: number | null;
}

export interface CotizacionResponse {
  idCotizacion: number;
  idCliente: number;
  clienteNombre: string;
  idTipoEvento: number;
  tipoEventoNombre: string;
  idUbicacion: number;
  direccionUbicacion: string;
  cantidadPersonas: number;
  fechaCotizacion: string;
  fechaEvento: string | null;
  presupuestoCliente: number | null;
  ultimaVersionId: number | null;
  ultimaVersionNumero: number | null;
  ultimaVersionEstado: string | null;
  ultimaVersionMonto: number | null;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface CotizacionVersionResponse {
  idCotizacionVersion: number;
  idCotizacion: number;
  idEstado: number;
  estadoNombre: string;
  numeroVersion: number;
  montoTotal: number;
  fechaVersion: string;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface DetalleCotizacionRequest {
  idMenu: number;
  idPlato: number;
  cantidadPlatos: number;
  observaciones: string | null;
}

export interface DetalleCotizacionResponse {
  idDetalleCotizacion: number;
  idCotizacionVersion: number;
  idMenu: number;
  nombreMenu: string;
  idPlato: number;
  nombrePlato: string;
  cantidadPlatos: number;
  precioUnitario: number;
  subtotal: number;
  observaciones: string | null;
  montoTotalVersion: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface ServicioCotizacionRequest {
  idTipoServicio: number;
  descripcion: string | null;
  monto: number;
}

export interface ServicioCotizacionResponse {
  idServicioCotizacion: number;
  idCotizacionVersion: number;
  idTipoServicio: number;
  tipoServicioNombre: string;
  descripcion: string | null;
  monto: number;
  montoTotalVersion: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

/** Transiciones validas por estado (espeja CotizacionVersionServiceImpl.TRANSICIONES_VALIDAS). */
export const TRANSICIONES_VALIDAS: Record<string, string[]> = {
  CREADA: ['ENVIADA'],
  ENVIADA: ['ACEPTADA', 'RECHAZADA'],
};
