/** Espeja cotizacion/dto/*.java del backend. */
export interface CotizacionRequest {
  idCliente: number;
  idTipoEvento: number;
  idUbicacion: number;
  cantidadPersonas: number;
  fechaEvento: string | null;
  presupuestoCliente: number | null;
  /** "18:00"; opcional al crear, obligatoria para enviar. */
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
  fechaCreacion: string | null;
  fechaModificacion: string | null;
}

export interface DetalleCotizacionRequest {
  idMenu: number;
  idPlato: number;
  cantidadPlatos: number;
  observaciones: string | null;
  /** Una de las bebidas que incluye el plato. */
  bebida: string | null;
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
  bebida: string | null;
  /** Bebidas que incluye el plato; si hay y bebida es null, falta elegirla. */
  bebidasPlato: string[];
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

/** Transiciones validas por estado (espeja CotizacionVersionServiceImpl.TRANSICIONES_VALIDAS). */
export const TRANSICIONES_VALIDAS: Record<string, string[]> = {
  CREADA: ['ENVIADA'],
  ENVIADA: ['ACEPTADA', 'RECHAZADA'],
};

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
