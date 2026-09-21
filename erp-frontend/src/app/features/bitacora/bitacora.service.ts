import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import {
  BitacoraAccesoResponse,
  BitacoraMovimientoResponse,
  FiltrosBitacoraAcceso,
  FiltrosBitacoraMovimiento,
} from './dto/bitacora';

const BASE_URL = `${API_URL}/bitacora`;

@Injectable({ providedIn: 'root' })
export class BitacoraService {
  constructor(private readonly http: HttpClient) {}

  listarMovimientos(filtros: FiltrosBitacoraMovimiento, page: number, size: number): Observable<Page<BitacoraMovimientoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (filtros.idUsuario) params = params.set('idUsuario', filtros.idUsuario);
    if (filtros.tabla) params = params.set('tabla', filtros.tabla);
    if (filtros.fechaDesde) params = params.set('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params = params.set('fechaHasta', filtros.fechaHasta);
    return this.http.get<Page<BitacoraMovimientoResponse>>(`${BASE_URL}/movimientos`, { params });
  }

  listarTablasConMovimientos(): Observable<string[]> {
    return this.http.get<string[]>(`${BASE_URL}/movimientos/tablas`);
  }

  listarAccesos(filtros: FiltrosBitacoraAcceso, page: number, size: number): Observable<Page<BitacoraAccesoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (filtros.idUsuario) params = params.set('idUsuario', filtros.idUsuario);
    if (filtros.resultado) params = params.set('resultado', filtros.resultado);
    if (filtros.fechaDesde) params = params.set('fechaDesde', filtros.fechaDesde);
    if (filtros.fechaHasta) params = params.set('fechaHasta', filtros.fechaHasta);
    return this.http.get<Page<BitacoraAccesoResponse>>(`${BASE_URL}/accesos`, { params });
  }
}
