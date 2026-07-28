/** Espeja administracion/rrhh/dto/*.java del backend. */
export interface EmpleadoRequest {
  idPuestoEmpleado: number;
  idEstado: number;
  idGenero: number | null;
  nombre: string;
  apellido: string;
  correo: string | null;
  telefono: string | null;
  fechaContratacion: string | null;
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
