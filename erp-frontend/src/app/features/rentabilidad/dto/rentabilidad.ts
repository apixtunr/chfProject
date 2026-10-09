/** Espeja rentabilidad/dto/*.java del backend. Solo eventos FINALIZADOS. */

/**
 * Rentabilidad de un evento, con las dos lecturas de la ganancia:
 * - acordada: lo que se vendio menos lo que costo (si el precio y los costos estuvieron bien).
 * - cobrada: lo que realmente entro menos lo que costo (el dinero que quedo).
 */
export interface RentabilidadEventoResponse {
  idEvento: number;
  fechaEvento: string;
  tipoEventoNombre: string;
  idCliente: number;
  clienteNombre: string;
  ingresosAcordados: number;
  cobrado: number;
  porCobrar: number;
  costoPersonal: number;
  costoInventario: number;
  costoExtra: number;
  totalCostos: number;
  gananciaAcordada: number;
  gananciaCobrada: number;
  margen: number;
}

export interface RentabilidadTipoResponse {
  tipoEventoNombre: string;
  cantidadEventos: number;
  ingresosAcordados: number;
  gananciaAcordada: number;
  margen: number;
}

export interface RentabilidadResumenResponse {
  cantidadEventos: number;
  ingresosAcordados: number;
  cobrado: number;
  porCobrar: number;
  costoPersonal: number;
  costoInventario: number;
  costoExtra: number;
  totalCostos: number;
  gananciaAcordada: number;
  gananciaCobrada: number;
  margen: number;
  porTipo: RentabilidadTipoResponse[];
}

export interface FiltrosRentabilidad {
  fechaDesde: string | null;
  fechaHasta: string | null;
  idCliente: number | null;
  idTipoEvento: number | null;
}
