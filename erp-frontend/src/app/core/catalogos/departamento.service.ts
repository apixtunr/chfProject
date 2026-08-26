import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-config';
import { DepartamentoResponse } from './departamento';

@Injectable({ providedIn: 'root' })
export class DepartamentoService {
  constructor(private readonly http: HttpClient) {}

  listar(): Observable<DepartamentoResponse[]> {
    return this.http.get<DepartamentoResponse[]>(`${API_URL}/departamentos`);
  }
}
