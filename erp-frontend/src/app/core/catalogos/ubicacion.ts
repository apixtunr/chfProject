/** Espeja ubicacion/dto/UbicacionResponse.java del backend. */
export interface UbicacionResponse {
  idUbicacion: number;
  idMunicipio: number;
  municipioNombre: string;
  departamentoNombre: string;
  direccion: string;
}

export interface UbicacionRequest {
  idMunicipio: number;
  direccion: string;
}
