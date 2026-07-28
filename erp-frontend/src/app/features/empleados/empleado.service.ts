import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import { EmpleadoRequest, EmpleadoResponse, GeneroResponse, PuestoEmpleadoResponse } from './dto/empleado';

const BASE_URL = `${API_URL}/empleados`;

@Injectable({ providedIn: 'root' })
export class EmpleadoService {
  constructor(private readonly http: HttpClient) {}

  listar(nombre: string, page: number, size: number): Observable<Page<EmpleadoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'apellido');
    if (nombre) {
      params = params.set('nombre', nombre);
    }
    return this.http.get<Page<EmpleadoResponse>>(BASE_URL, { params });
  }

  obtener(id: number): Observable<EmpleadoResponse> {
    return this.http.get<EmpleadoResponse>(`${BASE_URL}/${id}`);
  }

  crear(request: EmpleadoRequest): Observable<EmpleadoResponse> {
    return this.http.post<EmpleadoResponse>(BASE_URL, request);
  }

  actualizar(id: number, request: EmpleadoRequest): Observable<EmpleadoResponse> {
    return this.http.put<EmpleadoResponse>(`${BASE_URL}/${id}`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${id}`);
  }

  listarPuestos(): Observable<PuestoEmpleadoResponse[]> {
    return this.http.get<PuestoEmpleadoResponse[]>(`${API_URL}/puestos-empleado`);
  }

  listarGeneros(): Observable<GeneroResponse[]> {
    return this.http.get<GeneroResponse[]>(`${API_URL}/generos`);
  }
}
