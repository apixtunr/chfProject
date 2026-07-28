import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-config';
import { TipoCostoResponse } from './tipo-costo';

@Injectable({ providedIn: 'root' })
export class TipoCostoService {
  constructor(private readonly http: HttpClient) {}

  listar(): Observable<TipoCostoResponse[]> {
    return this.http.get<TipoCostoResponse[]>(`${API_URL}/tipos-costo`);
  }
}
