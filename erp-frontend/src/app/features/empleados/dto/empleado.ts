/** Espeja administracion/rrhh/dto/*.java del backend. */

/**
 * Acceso al sistema que se le crea al empleado al darlo de alta.
 *
 * Solo viaja en el alta y solo si la persona va a entrar al sistema. El backend graba
 * empleado y usuario en la misma transaccion: si el nombre de usuario ya esta tomado,
 * no se crea ninguno de los dos.
 */
export interface AccesoSistemaRequest {
  username: string;
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
