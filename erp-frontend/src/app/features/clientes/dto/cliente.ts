/** Espeja cliente/dto/ClienteRequest.java y ClienteResponse.java del backend. */
export interface ClienteRequest {
  nombre: string;
  correo: string | null;
  telefono: string | null;
  nit: string | null;
  direccion: string;
  idMunicipio: number;
}

export interface ClienteResponse {
  idCliente: number;
  nombre: string;
  correo: string | null;
  telefono: string | null;
  nit: string | null;
  direccion: string;
  idMunicipio: number;
  nombreMunicipio: string;
  /** Se deduce del municipio; el cliente no lo guarda aparte. */
  idDepartamento: number;
  nombreDepartamento: string;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}
