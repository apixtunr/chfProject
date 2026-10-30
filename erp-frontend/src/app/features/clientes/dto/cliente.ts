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
  idDepartamento: number;
  nombreDepartamento: string;
  idEstado: number;
  estadoNombre: 'ACTIVO' | 'INACTIVO' | string;
  activo: boolean;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export type EstadoClienteFiltro = 'ACTIVO' | 'INACTIVO' | 'TODOS';

export interface PosibleDuplicado {
  idCliente: number;
  nombre: string;
  nit: string;
  telefono: string | null;
  correo: string | null;
  activo: boolean;
  coincidencias: string[];
}
