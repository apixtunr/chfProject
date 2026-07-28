export interface RentabilidadEventoResponse {
  idEvento: number;
  fechaEvento: string;
  tipoEventoNombre: string;
  idCliente: number;
  clienteNombre: string;
  totalIngresos: number;
  totalCostos: number;
  ganancia: number;
  porcentaje: number;
}

export interface RentabilidadResumenResponse {
  cantidadEventos: number;
  totalIngresos: number;
  totalCostos: number;
  gananciaTotal: number;
  margenPromedio: number;
}

export interface FiltrosRentabilidad {
  fechaDesde: string | null;
  fechaHasta: string | null;
  idCliente: number | null;
  idTipoEvento: number | null;
}
