/** Espeja common/exception/ApiError.java del backend. */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  mensaje: string;
  path: string;
  detalles: string[] | null;
}
