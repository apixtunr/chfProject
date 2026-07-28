/** Espeja cliente/dto/ClienteRequest.java y ClienteResponse.java del backend. */
export interface ClienteRequest {
  nombre: string;
  correo: string | null;
  telefono: string | null;
  nit: string | null;
}

export interface ClienteResponse {
  idCliente: number;
  nombre: string;
  correo: string | null;
  telefono: string | null;
  nit: string | null;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}
