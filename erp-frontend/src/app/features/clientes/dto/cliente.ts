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
  idEstado: number;
  estadoNombre: 'ACTIVO' | 'INACTIVO' | string;
  /** Un cliente inactivo no se ofrece en cotizaciones ni eventos nuevos. */
  activo: boolean;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

/** Filtro de estado del listado. TODOS es el que usan reportes y filtros de historial. */
export type EstadoClienteFiltro = 'ACTIVO' | 'INACTIVO' | 'TODOS';

/** Espeja PosibleDuplicadoResponse.java: un cliente ya registrado que se parece al que se captura. */
export interface PosibleDuplicado {
  idCliente: number;
  nombre: string;
  nit: string;
  telefono: string | null;
  correo: string | null;
  activo: boolean;
  /** Que datos coinciden: "NIT", "teléfono", "correo", "nombre". */
  coincidencias: string[];
}
