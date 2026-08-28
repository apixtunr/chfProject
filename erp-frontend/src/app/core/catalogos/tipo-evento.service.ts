import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-config';
import { TipoEventoResponse } from './tipo-evento';

@Injectable({ providedIn: 'root' })
export class TipoEventoService {
  constructor(private readonly http: HttpClient) {}

  listar(): Observable<TipoEventoResponse[]> {
    return this.http.get<TipoEventoResponse[]>(`${API_URL}/tipos-evento`);
  }
}
