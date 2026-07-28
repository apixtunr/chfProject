export interface PlatoRequest {
  nombrePlato: string;
  idEstado: number;
}

export interface PlatoResponse {
  idPlato: number;
  nombrePlato: string;
  idEstado: number;
  estadoNombre: string;
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
  ordenMenu: number | null;
}

export interface MenuPlatoResponse {
  idMenu: number;
  idPlato: number;
  nombrePlato: string;
  ordenMenu: number;
  precioUnitario: number;
}
