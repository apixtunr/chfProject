/** Espeja menu/dto/MenuResponse.java del backend. */
export interface MenuResponse {
  idMenu: number;
  nombreMenu: string;
  idEstado: number;
  estadoNombre: string;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}
