import { inject } from '@angular/core';
import { Observable, map, of } from 'rxjs';
import { EstadoService } from '../../../core/catalogos/estado.service';
import { OpcionReporte } from './reporte-config';

export function estadosDe(tipoEstado: string): () => Observable<OpcionReporte[]> {
  return () =>
    inject(EstadoService)
      .listarPorTipo(tipoEstado)
      .pipe(map((estados) => estados.map((e) => ({ valor: e.idEstado, etiqueta: e.nombre }))));
}

export function fijas(opciones: OpcionReporte[]): () => Observable<OpcionReporte[]> {
  return () => of(opciones);
}
