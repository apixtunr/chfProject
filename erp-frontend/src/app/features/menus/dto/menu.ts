import { UnidadVenta } from '../../../core/catalogos/menu';

export interface PlatoRequest {
  nombrePlato: string;
  idEstado: number;
  unidadVenta: UnidadVenta;
  bebidas: string[];
}

export interface PlatoResponse {
  idPlato: number;
  nombrePlato: string;
  idEstado: number;
  estadoNombre: string;
  unidadVenta: UnidadVenta;
  bebidas: string[];
  fechaCreacion: string;
  fechaModificacion: string | null;
}

export interface MenuRequest {
  nombreMenu: string;
  idEstado: number;
}

export interface MenuResponse {
  idMenu: number;
  nombreMenu: string;
  idEstado: number;
  estadoNombre: string;
  fechaCreacion: string;
  fechaModificacion: string | null;
}

export interface MenuPlatoRequest {
  precioUnitario: number;
  precioDesde100: number | null;
  ordenMenu: number | null;
}

export interface MenuPlatoResponse {
  idMenu: number;
  idPlato: number;
  nombrePlato: string;
  ordenMenu: number;
  precioUnitario: number;
  precioDesde100: number | null;
  unidadVenta: UnidadVenta;
  bebidas: string[];
}
