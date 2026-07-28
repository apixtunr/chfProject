/** Espeja administracion/flota/dto/*.java del backend. */
export interface VehiculoRequest {
  idLineaVehiculo: number;
  idTipoPlaca: number;
  idEstado: number;
  placa: string;
  anioVehiculo: number | null;
}

export interface VehiculoResponse {
  idVehiculo: number;
  idLineaVehiculo: number;
  nombreLinea: string;
  nombreMarca: string;
  idTipoPlaca: number;
  tipoPlacaNombre: string;
  idEstado: number;
  estadoNombre: string;
  placa: string;
  anioVehiculo: number | null;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface LineaVehiculoResponse {
  idLineaVehiculo: number;
  idMarcaVehiculo: number;
  nombreMarca: string;
  nombreLinea: string;
}

export interface TipoPlacaResponse {
  idTipoPlaca: number;
  nombreTipo: string;
}
