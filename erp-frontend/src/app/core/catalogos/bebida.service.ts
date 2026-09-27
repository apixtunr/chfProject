import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-config';
import { Page } from '../models/page';
import { BebidaRequest, BebidaResponse } from './bebida';

@Injectable({ providedIn: 'root' })
export class BebidaService {
  private readonly http = inject(HttpClient);

  listar(nombre: string, page: number, size: number): Observable<Page<BebidaResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'nombreBebida');
    if (nombre) {
      params = params.set('nombre', nombre);
    }
    return this.http.get<Page<BebidaResponse>>(`${API_URL}/bebidas`, { params });
  }

  /** Las activas, para asignarlas a un plato. */
  listarActivas(): Observable<BebidaResponse[]> {
    return this.http.get<BebidaResponse[]>(`${API_URL}/bebidas/activas`);
  }

  obtener(id: number): Observable<BebidaResponse> {
    return this.http.get<BebidaResponse>(`${API_URL}/bebidas/${id}`);
  }

  crear(request: BebidaRequest): Observable<BebidaResponse> {
    return this.http.post<BebidaResponse>(`${API_URL}/bebidas`, request);
  }

  actualizar(id: number, request: BebidaRequest): Observable<BebidaResponse> {
    return this.http.put<BebidaResponse>(`${API_URL}/bebidas/${id}`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/bebidas/${id}`);
  }
}
