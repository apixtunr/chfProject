import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-config';
import { UbicacionRequest, UbicacionResponse } from './ubicacion';

const BASE_URL = `${API_URL}/ubicaciones`;

/** Salones/direcciones de eventos (propios o de terceros). Usado por Clientes y Eventos. */
@Injectable({ providedIn: 'root' })
export class UbicacionService {
  constructor(private readonly http: HttpClient) {}

  listar(idCliente: number | null): Observable<UbicacionResponse[]> {
    let params = new HttpParams();
    if (idCliente) {
      params = params.set('idCliente', idCliente);
    }
    return this.http.get<UbicacionResponse[]>(BASE_URL, { params });
  }

  crear(request: UbicacionRequest): Observable<UbicacionResponse> {
    return this.http.post<UbicacionResponse>(BASE_URL, request);
  }
}
