
export interface AccesoSistemaRequest {
  password: string;
  idRol: number;
}

export interface EmpleadoRequest {
  idPuestoEmpleado: number;
  idEstado: number;
  idGenero: number | null;
  nombre: string;
  apellido: string;
  correo: string | null;
  telefono: string | null;
  fechaContratacion: string | null;
  dpi: string;
  documentos: DocumentoItemRequest[];
  acceso?: AccesoSistemaRequest | null;
}

export interface EmpleadoResponse {
  idEmpleado: number;
  idPuestoEmpleado: number;
  puestoNombre: string;
  idEstado: number;
  estadoNombre: string;
  idGenero: number | null;
  generoNombre: string | null;
  nombre: string;
  apellido: string;
  correo: string | null;
  telefono: string | null;
  fechaContratacion: string | null;
  dpi: string | null;
  documentos: DocumentoEmpleadoResponse[] | null;
  idUsuario: number | null;
  username: string | null;
  rolUsuario: string | null;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface PuestoEmpleadoResponse {
  idPuestoEmpleado: number;
  nombreRol: string;
}

export interface GeneroResponse {
  idGenero: number;
  nombreGenero: string;
}

export interface DocumentoItemRequest {
  idTipoDocumento: number;
  numeroDocumento: string;
}

export interface DocumentoEmpleadoResponse {
  idEmpleado: number;
  idTipoDocumento: number;
  tipoDocumentoNombre: string;
  numeroDocumento: string;
}

export interface TipoDocumentoResponse {
  idTipoDocumento: number;
  nombreTipo: string;
}

export const TIPO_DPI = 'DPI';

export function formatoDpi(dpi: string | null | undefined): string {
  if (!dpi) {
    return '';
  }
  const d = dpi.replace(/\D/g, '');
  return d.length === 13 ? `${d.slice(0, 4)} ${d.slice(4, 9)} ${d.slice(9)}` : dpi;
}
