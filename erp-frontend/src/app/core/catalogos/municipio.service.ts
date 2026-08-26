import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api-config';
import { MunicipioResponse } from './municipio';

@Injectable({ providedIn: 'root' })
export class MunicipioService {
  constructor(private readonly http: HttpClient) {}

  listar(idDepartamento: number | null = null): Observable<MunicipioResponse[]> {
    let params = new HttpParams();
    if (idDepartamento) {
      params = params.set('idDepartamento', idDepartamento);
    }
    return this.http.get<MunicipioResponse[]>(`${API_URL}/municipios`, { params });
  }
}
