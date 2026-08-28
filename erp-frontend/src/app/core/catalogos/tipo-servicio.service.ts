import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-config';
import { TipoServicioResponse } from './tipo-servicio';

@Injectable({ providedIn: 'root' })
export class TipoServicioService {
  constructor(private readonly http: HttpClient) {}

  listar(): Observable<TipoServicioResponse[]> {
    return this.http.get<TipoServicioResponse[]>(`${API_URL}/tipos-servicio`);
  }
}
