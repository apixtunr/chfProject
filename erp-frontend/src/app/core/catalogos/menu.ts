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

/** Como se lee el precio de un plato (espeja entity/UnidadVenta.java). */
export type UnidadVenta = 'PERSONA' | 'CIENTO' | 'UNIDAD';

/** Como se escribe el precio de cada unidad de venta junto al monto: "Q800.00 el ciento". */
export const PRECIO_POR: Record<UnidadVenta, string> = {
  PERSONA: 'por persona',
  CIENTO: 'el ciento',
  UNIDAD: 'c/u',
};

/** Desde cuantas personas en el evento aplica el precio de volumen (MenuPlato.PERSONAS_PRECIO_VOLUMEN). */
export const PERSONAS_PRECIO_VOLUMEN = 100;

/** La empresa atiende eventos desde esta cantidad de personas (solo se avisa, no se bloquea). */
export const MINIMO_PERSONAS = 50;

/** Un plato especifico dentro de un menu, con su precio base y, si tiene, el de 100 personas o mas. */
export interface MenuPlatoResponse {
  idMenu: number;
  idPlato: number;
  nombrePlato: string;
  ordenMenu: number;
  /** Precio base (menos de 100 personas); para boquitas por ciento, el del ciento. */
  precioUnitario: number;
  /** Precio desde 100 personas; null = mismo precio base. */
  precioDesde100: number | null;
  unidadVenta: UnidadVenta;
  /** Bebidas que incluye el plato (vacio = ninguna); al cotizarlo se elige una. */
  bebidas: string[];
}

/** Bebidas que incluyen los platos del menu de la empresa. */
export const BEBIDAS = ['Té frío', 'Rosa de Jamaica', 'Atol de plátano', 'Jugo de naranja y café'];

/** Lo que cuesta una porcion o unidad en un evento de tantas personas (espeja MenuPlato.precioPorUnidadPara). */
export function precioPorUnidad(plato: MenuPlatoResponse, personasEvento: number): number {
  const precio =
    plato.precioDesde100 != null && personasEvento >= PERSONAS_PRECIO_VOLUMEN
      ? Number(plato.precioDesde100)
      : Number(plato.precioUnitario);
  return plato.unidadVenta === 'CIENTO' ? Math.round(precio) / 100 : precio;
}
