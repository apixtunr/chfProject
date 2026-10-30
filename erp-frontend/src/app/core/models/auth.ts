export interface LoginRequest {
  username: string;
  password: string;
}

export interface PermisoResponse {
  modulo: string;
  menuVista: string;
  opcion: string;
  paginaUrl: string | null;
  alta: boolean;
  baja: boolean;
  modificacion: boolean;
  imprimir: boolean;
  exportar: boolean;
}

export interface LoginResponse {
  token: string;
  username: string;
  nombreCompleto: string;
  rol: string;
  permisos: PermisoResponse[];
}

export type TipoPermiso = 'alta' | 'baja' | 'modificacion' | 'imprimir' | 'exportar';
