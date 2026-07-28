import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import { LineaVehiculoResponse, TipoPlacaResponse, VehiculoRequest, VehiculoResponse } from './dto/vehiculo';

const BASE_URL = `${API_URL}/vehiculos`;

@Injectable({ providedIn: 'root' })
export class VehiculoService {
  constructor(private readonly http: HttpClient) {}

  listar(page: number, size: number): Observable<Page<VehiculoResponse>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'placa');
    return this.http.get<Page<VehiculoResponse>>(BASE_URL, { params });
  }

  obtener(id: number): Observable<VehiculoResponse> {
    return this.http.get<VehiculoResponse>(`${BASE_URL}/${id}`);
  }

  crear(request: VehiculoRequest): Observable<VehiculoResponse> {
    return this.http.post<VehiculoResponse>(BASE_URL, request);
  }

  actualizar(id: number, request: VehiculoRequest): Observable<VehiculoResponse> {
    return this.http.put<VehiculoResponse>(`${BASE_URL}/${id}`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${id}`);
  }

  listarLineas(): Observable<LineaVehiculoResponse[]> {
    return this.http.get<LineaVehiculoResponse[]>(`${API_URL}/lineas-vehiculo`);
  }

  listarTiposPlaca(): Observable<TipoPlacaResponse[]> {
    return this.http.get<TipoPlacaResponse[]>(`${API_URL}/tipos-placa`);
  }
}
