import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import { FiltrosRentabilidad, RentabilidadEventoResponse, RentabilidadResumenResponse } from './dto/rentabilidad';

const BASE_URL = `${API_URL}/rentabilidad`;

@Injectable({ providedIn: 'root' })
export class RentabilidadService {
  constructor(private readonly http: HttpClient) {}

  listarPorEvento(filtros: FiltrosRentabilidad, page: number, size: number): Observable<Page<RentabilidadEventoResponse>> {
    const params = this.aParams(filtros).set('page', page).set('size', size).set('sort', 'fechaEvento,desc');
    return this.http.get<Page<RentabilidadEventoResponse>>(`${BASE_URL}/eventos`, { params });
  }

  resumen(filtros: FiltrosRentabilidad): Observable<RentabilidadResumenResponse> {
    return this.http.get<RentabilidadResumenResponse>(`${BASE_URL}/resumen`, { params: this.aParams(filtros) });
  }

  private aParams(filtros: FiltrosRentabilidad): HttpParams {
    let params = new HttpParams();
    if (filtros.fechaDesde) {
      params = params.set('fechaDesde', filtros.fechaDesde);
    }
    if (filtros.fechaHasta) {
      params = params.set('fechaHasta', filtros.fechaHasta);
    }
    if (filtros.idCliente) {
      params = params.set('idCliente', filtros.idCliente);
    }
    if (filtros.idTipoEvento) {
      params = params.set('idTipoEvento', filtros.idTipoEvento);
    }
    return params;
  }
}
