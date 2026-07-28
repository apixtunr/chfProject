export interface RolRequest {
  nombreRol: string;
}

export interface RolResponse {
  idRol: number;
  nombreRol: string;
}

export interface OpcionResponse {
  idOpcion: number;
  idMenuVista: number;
  menuVistaNombre: string;
  moduloNombre: string;
  nombreOpcion: string;
  ordenMenuVista: number;
  paginaUrl: string | null;
  accion: string | null;
}

export interface RolOpcionRequest {
  alta: boolean;
  baja: boolean;
  modificacion: boolean;
  imprimir: boolean;
  exportar: boolean;
}

export interface RolOpcionResponse extends RolOpcionRequest {
  idRol: number;
  nombreRol: string;
  idOpcion: number;
  nombreOpcion: string;
  paginaUrl: string | null;
}

export interface UsuarioRequest {
  username: string;
  password: string;
  idRol: number;
  idEstado: number;
  idEmpleado: number | null;
}

export interface UsuarioActualizarRequest {
  idRol: number;
  idEstado: number;
  idEmpleado: number | null;
}

export interface UsuarioResponse {
  idUsuario: number;
  username: string;
  idRol: number;
  nombreRol: string;
  idEstado: number;
  estadoNombre: string;
  idEmpleado: number | null;
  nombreEmpleado: string | null;
  intentosAcceso: number;
  fechaUltimoAcceso: string | null;
}
