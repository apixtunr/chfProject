import { BebidaResumen } from '../../../core/catalogos/bebida';
/** Espeja cotizacion/dto/*.java del backend. */
export interface CotizacionRequest {
  idCliente: number;
  idTipoEvento: number;
  idUbicacion: number;
  cantidadPersonas: number;
  fechaEvento: string | null;
  presupuestoCliente: number | null;
  /** "18:00": el servicio empieza en punto, de 11:00 a 19:00. */
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
  /** Calculada por el backend: 4 horas despues del inicio, sin pasar del cierre. */
  horaFin: string | null;
  ultimaVersionId: number | null;
  ultimaVersionNumero: number | null;
  ultimaVersionEstado: string | null;
  ultimaVersionMonto: number | null;
  /** Ultimo dia en que el cliente puede aceptar la ultima version (si se envio). */
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
  /** Cuando se envio al cliente; null mientras es borrador. */
  fechaEnvio: string | null;
  /** Ultimo dia en que el cliente puede aceptarla; despues pasa a VENCIDA. */
  vigenteHasta: string | null;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface DetalleCotizacionRequest {
  idMenu: number;
  idPlato: number;
  cantidadPlatos: number;
  observaciones: string | null;
  /** Una de las bebidas que incluye el plato (id del catalogo). */
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
  /** Bebidas que incluye el plato; si hay y no se eligio ninguna, falta elegirla. */
  bebidasPlato: BebidaResumen[];
  montoTotalVersion: number;
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface ServicioCotizacionRequest {
  idTipoServicio: number;
  descripcion: string | null;
  /** Unidades (ej. horas de cocinero); si el tipo tiene precio fijo, el total lo calcula el backend. */
  cantidad: number | null;
  /** Solo para tipos sin precio fijo. */
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

/**
 * Transiciones que elige el usuario (espeja CotizacionVersionServiceImpl.TRANSICIONES_VALIDAS).
 * VENCIDA la pone el sistema cuando pasa la vigencia y REEMPLAZADA al crear una version
 * nueva sobre una ENVIADA; no se eligen a mano.
 */
export const TRANSICIONES_VALIDAS: Record<string, string[]> = {
  CREADA: ['ENVIADA'],
  ENVIADA: ['ACEPTADA', 'RECHAZADA'],
};

/** Sobre que ultima version se puede crear otra (espeja PERMITEN_VERSION_NUEVA). */
export const PERMITEN_VERSION_NUEVA = ['ENVIADA', 'RECHAZADA', 'VENCIDA'];

/** Dias que le quedan a una cotizacion enviada: 0 = vence hoy, negativo = ya vencio. */
export function diasDeVigencia(vigenteHasta: string): number {
  const [anio, mes, dia] = vigenteHasta.substring(0, 10).split('-').map(Number);
  const hoy = new Date();
  const inicioHoy = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate());
  return Math.round((new Date(anio, mes - 1, dia).getTime() - inicioHoy.getTime()) / 86_400_000);
}

/** "vence hoy", "vence mañana", "vence en 5 días". */
export function textoVigencia(vigenteHasta: string): string {
  const dias = diasDeVigencia(vigenteHasta);
  if (dias < 0) return 'vencida';
  if (dias === 0) return 'vence hoy';
  if (dias === 1) return 'vence mañana';
  return `vence en ${dias} días`;
}

/** Anticipacion minima para agendar un evento (CondicionesComerciales.DIAS_ANTICIPACION). */
export const DIAS_ANTICIPACION = 7;

/** Primer dia que se puede elegir para un evento si se reserva hoy. */
export function fechaMinimaEvento(): Date {
  const minima = new Date();
  minima.setHours(0, 0, 0, 0);
  minima.setDate(minima.getDate() + DIAS_ANTICIPACION);
  return minima;
}

/** Horas en que puede empezar el servicio (CondicionesComerciales.HORAS_DE_INICIO). */
export const HORAS_DE_INICIO = [11, 12, 13, 14, 15, 16, 17, 18, 19].map((h) => `${String(h).padStart(2, '0')}:00`);

/** "18:00" o "18:00:00" -> "6:00 p.m." */
export function horaLegible(hora: string): string {
  const [h, m] = hora.split(':').map(Number);
  return `${h % 12 === 0 ? 12 : h % 12}:${String(m).padStart(2, '0')} ${h < 12 ? 'a.m.' : 'p.m.'}`;
}

/**
 * Fin del servicio de 4 horas: hasta las 21:00, o 22:00 si empieza a las 18 o 19
 * (espeja CondicionesComerciales.horaFinServicio).
 */
export function horaFinServicio(inicio: string): string {
  const h = Number(inicio.split(':')[0]);
  const fin = Math.min(h + 4, h >= 18 ? 22 : 21);
  return `${String(fin).padStart(2, '0')}:00`;
}

/** "6:00 p.m. a 10:00 p.m." */
export function horarioServicio(inicio: string): string {
  return `${horaLegible(inicio)} a ${horaLegible(horaFinServicio(inicio))}`;
}
