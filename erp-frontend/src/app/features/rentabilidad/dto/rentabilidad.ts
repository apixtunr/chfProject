
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
