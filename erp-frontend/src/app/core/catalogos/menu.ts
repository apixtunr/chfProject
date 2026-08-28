/** Espeja menu/dto/MenuResponse.java del backend. */
export interface MenuResponse {
  idMenu: number;
  nombreMenu: string;
  idEstado: number;
  estadoNombre: string;
  /** Suma de los precios de los platos del menu; solo informativo, no se usa para cotizar. */
  precioTotal: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

/** Un plato especifico dentro de un menu (categoria), con su propio precio. */
export interface MenuPlatoResponse {
  idMenu: number;
  idPlato: number;
  nombrePlato: string;
  ordenMenu: number;
  precioUnitario: number;
}
