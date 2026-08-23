/** Espeja cotizacion/dto/*.java del backend. */
export interface CotizacionRequest {
  idCliente: number;
  fechaEvento: string | null;
  presupuestoCliente: number | null;
}

export interface CotizacionResponse {
  idCotizacion: number;
  idCliente: number;
  clienteNombre: string;
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
  cantidadPlatos: number;
  precioUnitario: number;
  observaciones: string | null;
}

export interface DetalleCotizacionResponse {
  idDetalleCotizacion: number;
  idCotizacionVersion: number;
  idMenu: number;
  nombreMenu: string;
  cantidadPlatos: number;
  precioUnitario: number;
  subtotal: number;
  observaciones: string | null;
  montoTotalVersion: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

/** Transiciones validas por estado (espeja CotizacionVersionServiceImpl.TRANSICIONES_VALIDAS). */
export const TRANSICIONES_VALIDAS: Record<string, string[]> = {
  CREADA: ['ENVIADA'],
  ENVIADA: ['ACEPTADA', 'RECHAZADA'],
};
