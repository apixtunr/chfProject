import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-config';
import { UbicacionRequest, UbicacionResponse } from './ubicacion';

const BASE_URL = `${API_URL}/ubicaciones`;

/** Salones/direcciones de eventos (propios o de terceros). Usado por Cotizaciones y Eventos. */
@Injectable({ providedIn: 'root' })
export class UbicacionService {
  constructor(private readonly http: HttpClient) {}

  listar(): Observable<UbicacionResponse[]> {
    return this.http.get<UbicacionResponse[]>(BASE_URL);
  }

  obtener(id: number): Observable<UbicacionResponse> {
    return this.http.get<UbicacionResponse>(`${BASE_URL}/${id}`);
  }

  actualizar(id: number, request: UbicacionRequest): Observable<UbicacionResponse> {
    return this.http.put<UbicacionResponse>(`${BASE_URL}/${id}`, request);
  }

  crear(request: UbicacionRequest): Observable<UbicacionResponse> {
    return this.http.post<UbicacionResponse>(BASE_URL, request);
  }
}
