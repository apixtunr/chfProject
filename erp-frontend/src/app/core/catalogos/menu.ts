import { BebidaResumen } from './bebida';

export interface MenuResponse {
  idMenu: number;
  nombreMenu: string;
  idEstado: number;
  estadoNombre: string;
  precioTotal: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export type UnidadVenta = 'PERSONA' | 'CIENTO' | 'UNIDAD';

export const PRECIO_POR: Record<UnidadVenta, string> = {
  PERSONA: 'por persona',
  CIENTO: 'el ciento',
  UNIDAD: 'c/u',
};

export const PERSONAS_PRECIO_VOLUMEN = 100;

export const MINIMO_PERSONAS = 50;

export interface MenuPlatoResponse {
  idMenu: number;
  idPlato: number;
  nombrePlato: string;
  ordenMenu: number;
  precioUnitario: number;
  precioDesde100: number | null;
  unidadVenta: UnidadVenta;
  bebidas: BebidaResumen[];
}

export function precioPorUnidad(plato: MenuPlatoResponse, personasEvento: number): number {
  const precio =
    plato.precioDesde100 != null && personasEvento >= PERSONAS_PRECIO_VOLUMEN
      ? Number(plato.precioDesde100)
      : Number(plato.precioUnitario);
  return plato.unidadVenta === 'CIENTO' ? Math.round(precio) / 100 : precio;
}
