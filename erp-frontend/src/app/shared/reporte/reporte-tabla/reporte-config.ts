import { Observable } from 'rxjs';

export interface OpcionReporte {
  valor: string | number;
  etiqueta: string;
}

export interface FiltroReporte {
  clave: string;
  etiqueta: string;
  icono: string;
  tipo: 'cliente' | 'lista';
  todos?: string;
  opciones?: () => Observable<OpcionReporte[]>;
}

export interface ConfigReporte {
  titulo: string;
  subtitulo: string;
  icono: string;
  paginaUrl: string;
  url: string;
  archivo: string;
  fechas: { porDefecto: 'mes' | 'ninguna'; etiqueta?: string };
  fechaSegun?: OpcionReporte[];
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

export const AGRUPACIONES_FECHA: OpcionReporte[] = [
  { valor: 'DIA', etiqueta: 'Por día' },
  { valor: 'SEMANA', etiqueta: 'Por semana' },
  { valor: 'MES', etiqueta: 'Por mes' },
  { valor: 'ANIO', etiqueta: 'Por año' },
];
