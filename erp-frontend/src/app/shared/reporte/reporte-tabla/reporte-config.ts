import { Observable } from 'rxjs';

export interface OpcionReporte {
  valor: string | number;
  etiqueta: string;
}

/**
 * Un filtro del reporte. clave es el nombre del parametro que recibe la API.
 * - cliente: campo autocompletable que busca clientes por nombre.
 * - lista: desplegable; opciones se ejecuta dentro del contexto de inyeccion, asi que
 *   puede usar inject() para pedir las opciones a un servicio.
 */
export interface FiltroReporte {
  clave: string;
  etiqueta: string;
  icono: string;
  tipo: 'cliente' | 'lista';
  todos?: string;
  opciones?: () => Observable<OpcionReporte[]>;
}

/** Configuracion de un reporte por modulo: lo unico que cambia de uno a otro. */
export interface ConfigReporte {
  titulo: string;
  subtitulo: string;
  icono: string;
  /** Pagina del modulo, para los permisos de imprimir y exportar. */
  paginaUrl: string;
  /** Endpoint relativo a /api (el PDF es el mismo con /pdf al final). */
  url: string;
  /** Nombre base del archivo CSV y del PDF. */
  archivo: string;
  /** Filtro de fechas: con que periodo abre y, si hace falta, que fecha se filtra. */
  fechas: { porDefecto: 'mes' | 'ninguna'; etiqueta?: string };
  /** Para elegir por cual fecha se filtra (por ejemplo, la del evento o la de registro). */
  fechaSegun?: OpcionReporte[];
  /** Agrupaciones propias del reporte, ademas de dia, semana, mes y año. */
  agrupaciones?: OpcionReporte[];
  filtros: FiltroReporte[];
  sinDatos: string;
}

export type TipoColumna = 'TEXTO' | 'NUMERO' | 'MONTO' | 'FECHA' | 'FECHA_HORA';

export interface ColumnaReporte {
  clave: string;
  titulo: string;
  tipo: TipoColumna;
  sumar: boolean;
}

export interface FilaReporte {
  valores: Record<string, string | number | null>;
  excluida: boolean;
}

export interface GrupoReporte {
  etiqueta: string | null;
  cantidad: number;
  filas: FilaReporte[];
  subtotales: Record<string, number>;
}

export interface ReporteTabla {
  titulo: string;
  columnas: ColumnaReporte[];
  grupos: GrupoReporte[];
  totales: Record<string, number>;
  cantidad: number;
  excluidas: number;
}

/** Agrupaciones por fecha, comunes a todos los reportes. */
export const AGRUPACIONES_FECHA: OpcionReporte[] = [
  { valor: 'DIA', etiqueta: 'Por día' },
  { valor: 'SEMANA', etiqueta: 'Por semana' },
  { valor: 'MES', etiqueta: 'Por mes' },
  { valor: 'ANIO', etiqueta: 'Por año' },
];
