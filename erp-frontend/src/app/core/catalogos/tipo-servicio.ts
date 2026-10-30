export interface TipoServicioResponse {
  idTipoServicio: number;
  nombreTipo: string;
  descripcion: string | null;
  precioUnitario: number | null;
  activo: boolean;
}
