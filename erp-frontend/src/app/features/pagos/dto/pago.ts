export interface MetodoPagoResponse {
  idMetodoPago: number;
  idEstado: number;
  estadoNombre: string;
  nombreMetodo: string;
  descripcion: string | null;
  requiereReferencia: boolean;
}

export interface PagoRequest {
  idEvento: number;
  idMetodoPago: number;
  monto: number;
  referenciaTransaccion: string | null;
  observaciones: string | null;
  fechaPago: string | null;
}

export interface PagoResponse {
  idPago: number;
  idEvento: number;
  idUsuario: number;
  usernameUsuario: string;
  idMetodoPago: number;
  nombreMetodoPago: string;
  idEstado: number;
  estadoNombre: string;
  monto: number;
  referenciaTransaccion: string | null;
  observaciones: string | null;
  fechaPago: string;
  fechaCreacion: string;
  fechaModificacion: string | null;
}

export interface ComprobantePagoRequest {
  numeroComprobante: string;
  archivoUrl: string | null;
  tipoComprobante: string | null;
  fechaEmision: string | null;
  esValido: boolean;
}

export interface ComprobantePagoResponse {
  idComprobante: number;
  idPago: number;
  numeroComprobante: string;
  archivoUrl: string | null;
  tipoComprobante: string | null;
  fechaEmision: string | null;
  esValido: boolean;
}
