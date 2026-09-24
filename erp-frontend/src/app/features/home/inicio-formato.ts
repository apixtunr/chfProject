/**
 * Formato de fechas, horas y montos para la pantalla de inicio. Se usa Intl con 'es-GT'
 * en vez de DatePipe porque la app no registra los datos de locale de Angular en espanol
 * (DatePipe devolveria "Sep" / "Wednesday").
 *
 * Las fechas "yyyy-MM-dd" se interpretan como fecha LOCAL, no UTC: new Date('2026-09-23')
 * es medianoche UTC, que en Guatemala todavia es el 22.
 */

export function aFecha(iso: string): Date {
  const [anio, mes, dia] = iso.substring(0, 10).split('-').map(Number);
  return new Date(anio, mes - 1, dia);
}

export function fechaLocalHoy(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

/** Dias entre hoy y la fecha dada (negativo si ya paso). */
export function diasHasta(fecha: string, hoy: string): number {
  return Math.round((aFecha(fecha).getTime() - aFecha(hoy).getTime()) / 86_400_000);
}

function capitalizar(texto: string): string {
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

export function fechaLarga(fecha: string): string {
  return capitalizar(new Intl.DateTimeFormat('es-GT', { weekday: 'long', day: 'numeric', month: 'long' }).format(aFecha(fecha)));
}

export function fechaCorta(fecha: string | null): string {
  if (!fecha) return '';
  return new Intl.DateTimeFormat('es-GT', { day: 'numeric', month: 'short' }).format(aFecha(fecha)).replace('.', '');
}

export function mesCorto(fecha: string): string {
  return new Intl.DateTimeFormat('es-GT', { month: 'short' }).format(aFecha(fecha)).replace('.', '');
}

/** "Hoy", "Mañana" o el dia de la semana. */
export function nombreDia(fecha: string, hoy: string): string {
  const dias = diasHasta(fecha, hoy);
  if (dias === 0) return 'Hoy';
  if (dias === 1) return 'Mañana';
  return capitalizar(new Intl.DateTimeFormat('es-GT', { weekday: 'long' }).format(aFecha(fecha)));
}

/** "14:30:00" -> "14:30". */
export function hora(h: string | null): string {
  return h ? h.substring(0, 5) : '';
}

export function monto(valor: number): string {
  return new Intl.NumberFormat('es-GT', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(valor);
}

/** "ahora", "hace 5 min", "hace 2 h", "ayer", "hace 3 días". */
export function haceCuanto(fechaHora: string | null): string {
  if (!fechaHora) return '';
  const minutos = Math.floor((Date.now() - new Date(fechaHora).getTime()) / 60_000);
  if (minutos < 1) return 'ahora';
  if (minutos < 60) return `hace ${minutos} min`;
  const horas = Math.floor(minutos / 60);
  if (horas < 24) return `hace ${horas} h`;
  const dias = Math.floor(horas / 24);
  return dias === 1 ? 'ayer' : `hace ${dias} días`;
}

export function porcentaje(parte: number, total: number): number {
  return total > 0 ? Math.max(0, Math.min(100, Math.round((parte / total) * 100))) : 0;
}

export function iniciales(nombre: string | null): string {
  if (!nombre) return '?';
  return nombre
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('');
}
