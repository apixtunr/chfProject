import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { API_URL } from '../api-config';
import { EstadoResponse } from './estado';

/** Catalogo generico de estados (usado por Cotizaciones, Eventos, Pagos, etc.). */
@Injectable({ providedIn: 'root' })
export class EstadoService {
  constructor(private readonly http: HttpClient) {}

  listarPorTipo(tipoEstadoNombre: string): Observable<EstadoResponse[]> {
    return this.http
      .get<EstadoResponse[]>(`${API_URL}/estados`)
      .pipe(map((estados) => estados.filter((e) => e.tipoEstadoNombre === tipoEstadoNombre)));
  }
}
