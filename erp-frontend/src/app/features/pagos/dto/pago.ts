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
  /** Si viene con valor, este pago es el reembolso de ese costo extra, no un abono al menu. */
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

/** Saldo de un evento (total/abonado/pendiente), para la pantalla principal de Pagos. */
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

/** Los mismos que acepta el backend (ComprobantePagoServiceImpl.TIPOS_COMPROBANTE). */
export const TIPOS_COMPROBANTE = [
  'Boleta de depósito',
  'Voucher de tarjeta',
  'Comprobante de transferencia',
  'Factura',
  'Recibo',
] as const;

/** Lo que se acepta como archivo del comprobante (el backend lo vuelve a revisar). */
export const ARCHIVOS_COMPROBANTE = '.pdf,.jpg,.jpeg,.png,application/pdf,image/jpeg,image/png';
export const TAMANO_MAXIMO_COMPROBANTE = 5 * 1024 * 1024;

export interface ComprobantePagoRequest {
  numeroComprobante: string;
  /** La llena el sistema al guardar el archivo; se manda null. */
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
  /** Datos del archivo adjunto; los tres en null si no tiene. */
  nombreArchivo: string | null;
  tipoContenido: string | null;
  tamanoBytes: number | null;
}
