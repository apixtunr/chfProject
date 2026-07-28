export interface CategoriaProductoResponse {
  idCategoria: number;
  idTipoInventario: number;
  nombreTipo: string;
  nombreCategoria: string;
}

export interface ProductoRequest {
  idCategoria: number;
  nombreProducto: string;
  unidadMedida: string;
  precioUnitario: number;
}

export interface ProductoResponse {
  idProducto: number;
  idCategoria: number;
  nombreCategoria: string;
  nombreProducto: string;
  unidadMedida: string;
  precioUnitario: number;
  fechaCreacion: string;
  fechaModificacion: string | null;
}

export interface InventarioResponse {
  idInventario: number;
  idProducto: number;
  nombreProducto: string;
  cantidadTotal: number;
  cantidadMinima: number;
  fechaCreacion: string;
  fechaModificacion: string | null;
}

export type TipoMovimiento = 'ENTRADA' | 'SALIDA' | 'AJUSTE';

export interface MovimientoInventarioRequest {
  idProducto: number;
  tipoMovimiento: TipoMovimiento;
  /** ENTRADA/SALIDA: magnitud positiva. AJUSTE: valor con signo. */
  cantidad: number;
  descripcion: string | null;
  idEvento: number | null;
}

export interface MovimientoInventarioResponse {
  idMovimiento: number;
  idProducto: number;
  nombreProducto: string;
  idEvento: number | null;
  idUsuario: number | null;
  usernameUsuario: string | null;
  tipoMovimiento: TipoMovimiento;
  cantidad: number;
  descripcion: string | null;
  fechaMovimiento: string;
  cantidadTotalActual: number;
}
