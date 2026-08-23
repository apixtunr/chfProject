import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import { ClienteRequest, ClienteResponse } from './dto/cliente';

const BASE_URL = `${API_URL}/clientes`;

@Injectable({ providedIn: 'root' })
export class ClienteService {
  constructor(private readonly http: HttpClient) {}

  listar(nombre: string, page: number, size: number): Observable<Page<ClienteResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'idCliente');
    if (nombre) {
      params = params.set('nombre', nombre);
    }
    return this.http.get<Page<ClienteResponse>>(BASE_URL, { params });
  }

  obtener(id: number): Observable<ClienteResponse> {
    return this.http.get<ClienteResponse>(`${BASE_URL}/${id}`);
  }

  crear(request: ClienteRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(BASE_URL, request);
  }

  actualizar(id: number, request: ClienteRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`${BASE_URL}/${id}`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${id}`);
  }
}
