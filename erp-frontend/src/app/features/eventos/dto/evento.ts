import { BebidaResumen } from '../../../core/catalogos/bebida';
export interface EventoRequest {
  idCotizacionVersion: number | null;
  idCliente: number | null;
  idTipoEvento: number | null;
  idUbicacion: number | null;
  fechaEvento: string | null;
  horaInicio: string | null;
  horaFin: string | null;
  cantidadPersonas: number | null;
  observaciones: string | null;
}

export interface EventoResponse {
  idEvento: number;
  idCotizacionVersion: number | null;
  idCliente: number;
  clienteNombre: string;
  idTipoEvento: number;
  tipoEventoNombre: string;
  idUbicacion: number;
  direccionUbicacion: string;
  idEstado: number;
  estadoNombre: string;
  fechaEvento: string;
  horaInicio: string | null;
  horaFin: string | null;
  cantidadPersonas: number | null;
  observaciones: string | null;
  montoMenu: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
  planificadoSinAnticipo: boolean;
  motivoCancelacion: string | null;
  acuerdoAnticipo: AcuerdoAnticipo | null;
  montoDevuelto: number | null;
}

export type AcuerdoAnticipo = 'SIN_ANTICIPO' | 'RETENIDO' | 'DEVUELTO' | 'DEVUELTO_PARCIAL';

export const ACUERDOS_ANTICIPO: Record<AcuerdoAnticipo, string> = {
  SIN_ANTICIPO: 'No había pagos',
  RETENIDO: 'Se retiene lo pagado',
  DEVUELTO: 'Se devuelve todo',
  DEVUELTO_PARCIAL: 'Se devuelve una parte',
};

export interface AnticipoResponse {
  total: number;
  abonado: number;
  anticipoRequerido: number;
  faltante: number;
  cubierto: boolean;
}

export interface CancelarEventoRequest {
  motivo: string;
  acuerdoAnticipo: AcuerdoAnticipo | null;
  montoDevuelto: number | null;
}

export interface ConteoResponse {
  etiqueta: string;
  cantidad: number;
}

export interface EventoResumenResponse {
  totalEventos: number;
  personasAtendidas: number;
  porEstado: ConteoResponse[];
  porTipo: ConteoResponse[];
}

export interface FiltrosEvento {
  fechaDesde: string | null;
  fechaHasta: string | null;
  idCliente: number | null;
  idTipoEvento: number | null;
  idEstado: number | null;
}

export interface DetalleEventoRequest {
  idMenu: number;
  idPlato: number;
  cantidadPlatos: number;
  observaciones: string | null;
  idBebida: number | null;
}

export interface DetalleEventoResponse {
  idDetalleEvento: number;
  idEvento: number;
  idMenu: number;
  nombreMenu: string;
  idPlato: number;
  nombrePlato: string;
  cantidadPlatos: number;
  precioUnitario: number;
  subtotal: number;
  observaciones: string | null;
  idBebida: number | null;
  nombreBebida: string | null;
  bebidasPlato: BebidaResumen[];
  montoMenuEvento: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface CostoEventoRequest {
  idTipoCosto: number;
  descripcion: string | null;
  monto: number;
  fechaCosto: string | null;
}

export interface CostoEventoResponse {
  idCostoEvento: number;
  idEvento: number;
  idTipoCosto: number;
  tipoCostoNombre: string;
  descripcion: string | null;
  monto: number;
  fechaCosto: string;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
  pagado: boolean;
}

export interface EventoEmpleadoRequest {
  salarioEvento: number;
  horaInicio: string | null;
  horaFin: string | null;
  idEstado: number | null;
}

export interface EventoEmpleadoResponse {
  idEvento: number;
  idEmpleado: number;
  nombreEmpleado: string;
  idEstado: number;
  estadoNombre: string;
  salarioEvento: number;
  fechaAsignacion: string;
  horaInicio: string | null;
  horaFin: string | null;
  tieneLicencia: boolean | null;
}

export interface EventoVehiculoRequest {
  idEmpleadoConductor: number | null;
}

export interface EventoVehiculoResponse {
  idEvento: number;
  idVehiculo: number;
  placaVehiculo: string;
  idEmpleadoConductor: number | null;
  nombreConductor: string | null;
  fechaAsignacion: string;
}

export interface EventoInventarioRequest {
  cantidad: number;
  fechaConsumo: string | null;
}

export interface EventoInventarioResponse {
  idEvento: number;
  idProducto: number;
  nombreProducto: string;
  cantidad: number;
  fechaConsumo: string | null;
}

export interface EventoInventarioCorreccionRequest {
  cantidadCorrecta: number;
}

export interface EventoInventarioFalloResponse {
  nombreProducto: string;
  motivo: string;
}

export interface EventoInventarioConfirmacionMasivaResponse {
  confirmados: EventoInventarioResponse[];
  fallidos: EventoInventarioFalloResponse[];
}

export interface TipoEventoResponse {
  idTipoEvento: number;
  nombreTipo: string;
}

export interface ColorEstado {
  bg: string;
  text: string;
}

export const COLOR_POR_ESTADO_EVENTO: Record<string, ColorEstado> = {
  CREADO: { bg: '#F3F4F6', text: '#374151' },
  FINALIZADO: { bg: '#ECFDF5', text: '#047857' },
  CANCELADO: { bg: '#FFF1F2', text: '#BE123C' },
  PLANIFICADO: { bg: '#EFF6FF', text: '#1D4ED8' },
  'EN CURSO': { bg: '#FFFBEB', text: '#92400E' },
};
export const COLOR_ESTADO_EVENTO_DEFECTO: ColorEstado = { bg: '#F3F4F6', text: '#374151' };
