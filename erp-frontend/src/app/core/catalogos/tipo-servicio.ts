/** Espeja administracion/catalogo/dto/TipoServicioResponse.java del backend. */
export interface TipoServicioResponse {
  idTipoServicio: number;
  nombreTipo: string;
  descripcion: string | null;
  /** Precio por unidad (ej. por cocinero y hora); null = monto libre al cotizar. */
  precioUnitario: number | null;
}
