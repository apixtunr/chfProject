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
  idCostoEvento: number | null;
}

export interface PagoResponse {
  idPago: number;
  idEvento: number;
  clienteNombre: string;
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
  idCostoEvento: number | null;
}

export interface EventoPagoResponse {
  idEvento: number;
  fechaEvento: string;
  idEstado: number;
  estadoNombre: string;
  tipoEventoNombre: string;
  idCliente: number;
  clienteNombre: string;
  total: number;
  abonado: number;
  pendiente: number;
}

export const TIPOS_COMPROBANTE = [
  'Boleta de depósito',
  'Voucher de tarjeta',
  'Comprobante de transferencia',
  'Factura',
  'Recibo',
] as const;

export const ARCHIVOS_COMPROBANTE = '.pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png';
export const TAMANO_MAXIMO_COMPROBANTE = 5 * 1024 * 1024;

export interface ComprobantePagoRequest {
  numeroComprobante: string;
  archivoUrl: string | null;
  tipoComprobante: string;
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
  nombreArchivo: string | null;
  tipoContenido: string | null;
  tamanoBytes: number | null;
}
