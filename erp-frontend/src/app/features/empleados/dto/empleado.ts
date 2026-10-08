/** Espeja administracion/rrhh/dto/*.java del backend. */

/**
 * Acceso al sistema que se le crea al empleado al darlo de alta.
 *
 * Solo viaja en el alta y solo si la persona va a entrar al sistema. El backend graba
 * empleado y usuario en la misma transaccion. El nombre de usuario no se envia: lo genera
 * el backend con el nombre del empleado (nombre.apellido).
 */
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
  /** Obligatorio, 13 digitos; se aceptan los espacios del documento fisico. */
  dpi: string;
  /** Los demas documentos; la lista reemplaza a los que tenga registrados. */
  documentos: DocumentoItemRequest[];
  /** null cuando la persona no va a entrar al sistema, que es el caso habitual. */
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
  /** Sin espacios; null en empleados registrados antes de que fuera obligatorio. */
  dpi: string | null;
  /** Los demas documentos; solo viene al consultar un empleado, no en el listado. */
  documentos: DocumentoEmpleadoResponse[] | null;
  /** Los tres van juntos: o la persona tiene usuario, o los tres vienen en null. */
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

/** Tipos que el sistema reconoce por nombre (V31). */
export const TIPO_DPI = 'DPI';

/** "2547123450101" -> "2547 12345 0101", como se lee en el documento fisico. */
export function formatoDpi(dpi: string | null | undefined): string {
  if (!dpi) {
    return '';
  }
  const d = dpi.replace(/\D/g, '');
  return d.length === 13 ? `${d.slice(0, 4)} ${d.slice(4, 9)} ${d.slice(9)}` : dpi;
}
