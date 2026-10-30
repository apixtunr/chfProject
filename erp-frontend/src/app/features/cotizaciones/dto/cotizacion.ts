import { BebidaResumen } from '../../../core/catalogos/bebida';
export interface CotizacionRequest {
  idCliente: number;
  idTipoEvento: number;
  idUbicacion: number;
  cantidadPersonas: number;
  fechaEvento: string | null;
  presupuestoCliente: number | null;
  horaInicio: string | null;
}

export interface CotizacionResponse {
  idCotizacion: number;
  idCliente: number;
  clienteNombre: string;
  idTipoEvento: number;
  tipoEventoNombre: string;
  idUbicacion: number;
  direccionUbicacion: string;
  cantidadPersonas: number;
  fechaCotizacion: string;
  fechaEvento: string | null;
  presupuestoCliente: number | null;
  horaInicio: string | null;
  horaFin: string | null;
  ultimaVersionId: number | null;
  ultimaVersionNumero: number | null;
  ultimaVersionEstado: string | null;
  ultimaVersionMonto: number | null;
  ultimaVersionVigenteHasta: string | null;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface CotizacionVersionResponse {
  idCotizacionVersion: number;
  idCotizacion: number;
  idEstado: number;
  estadoNombre: string;
  numeroVersion: number;
  montoTotal: number;
  fechaVersion: string;
  fechaEnvio: string | null;
  vigenteHasta: string | null;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface DetalleCotizacionRequest {
  idMenu: number;
  idPlato: number;
  cantidadPlatos: number;
  observaciones: string | null;
  idBebida: number | null;
}

export interface DetalleCotizacionResponse {
  idDetalleCotizacion: number;
  idCotizacionVersion: number;
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
  montoTotalVersion: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface ServicioCotizacionRequest {
  idTipoServicio: number;
  descripcion: string | null;
  cantidad: number | null;
  monto: number | null;
}

export interface ServicioCotizacionResponse {
  idServicioCotizacion: number;
  idCotizacionVersion: number;
  idTipoServicio: number;
  tipoServicioNombre: string;
  descripcion: string | null;
  cantidad: number;
  precioUnitario: number;
  monto: number;
  montoTotalVersion: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export const TRANSICIONES_VALIDAS: Record<string, string[]> = {
  CREADA: ['ENVIADA'],
  ENVIADA: ['ACEPTADA', 'RECHAZADA'],
};

export const PERMITEN_VERSION_NUEVA = ['ENVIADA', 'RECHAZADA', 'VENCIDA'];

export function diasDeVigencia(vigenteHasta: string): number {
  const [anio, mes, dia] = vigenteHasta.substring(0, 10).split('-').map(Number);
  const hoy = new Date();
  const inicioHoy = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
  return Math.round((new Date(anio, mes - 1, dia).getTime() - inicioHoy.getTime()) / 86_400_000);
}

export function textoVigencia(vigenteHasta: string): string {
  const dias = diasDeVigencia(vigenteHasta);
  if (dias < 0) return 'vencida';
  if (dias === 0) return 'vence hoy';
  if (dias === 1) return 'vence mañana';
  return `vence en ${dias} días`;
}

export const DIAS_ANTICIPACION = 7;

export function fechaMinimaEvento(): Date {
  const minima = new Date();
  minima.setHours(0, 0, 0, 0);
  minima.setDate(minima.getDate() + DIAS_ANTICIPACION);
  return minima;
}

export const HORAS_DE_INICIO = [7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19].map((h) => `${String(h).padStart(2, '0')}:00`);

export function horaLegible(hora: string): string {
  const [h, m] = hora.split(':').map(Number);
  return `${h % 12 === 0 ? 12 : h % 12}:${String(m).padStart(2, '0')} ${h < 12 ? 'a.m.' : 'p.m.'}`;
}

export function horaFinServicio(inicio: string): string {
  const h = Number(inicio.split(':')[0]);
  const fin = Math.min(h + 4, h < 11 ? 11 : h >= 18 ? 22 : 21);
  return `${String(fin).padStart(2, '0')}:00`;
}

export function horarioServicio(inicio: string): string {
  return `${horaLegible(inicio)} a ${horaLegible(horaFinServicio(inicio))}`;
}
