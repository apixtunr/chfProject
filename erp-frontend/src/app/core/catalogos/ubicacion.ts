/** Espeja cliente/dto/UbicacionResponse.java del backend. */
export interface UbicacionResponse {
  idUbicacion: number;
  idCliente: number | null;
  clienteNombre: string | null;
  idMunicipio: number;
  municipioNombre: string;
  departamentoNombre: string;
  direccion: string;
}

export interface UbicacionRequest {
  idCliente: number | null;
  idMunicipio: number;
  direccion: string;
}
