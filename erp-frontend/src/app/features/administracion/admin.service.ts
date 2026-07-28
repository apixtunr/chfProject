import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import {
  OpcionResponse,
  RolOpcionRequest,
  RolOpcionResponse,
  RolRequest,
  RolResponse,
  UsuarioActualizarRequest,
  UsuarioRequest,
  UsuarioResponse,
} from './dto/admin';

@Injectable({ providedIn: 'root' })
export class AdminService {
  constructor(private readonly http: HttpClient) {}

  // --- Roles ---

  listarRoles(): Observable<RolResponse[]> {
    return this.http.get<RolResponse[]>(`${API_URL}/roles`);
  }

  crearRol(request: RolRequest): Observable<RolResponse> {
    return this.http.post<RolResponse>(`${API_URL}/roles`, request);
  }

  actualizarRol(id: number, request: RolRequest): Observable<RolResponse> {
    return this.http.put<RolResponse>(`${API_URL}/roles/${id}`, request);
  }

  eliminarRol(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/roles/${id}`);
  }

  // --- Opciones y permisos ---

  listarOpciones(): Observable<OpcionResponse[]> {
    return this.http.get<OpcionResponse[]>(`${API_URL}/opciones`);
  }

  listarPermisosDeRol(idRol: number): Observable<RolOpcionResponse[]> {
    return this.http.get<RolOpcionResponse[]>(`${API_URL}/roles/${idRol}/permisos`);
  }

  asignarPermiso(idRol: number, idOpcion: number, request: RolOpcionRequest): Observable<RolOpcionResponse> {
    return this.http.post<RolOpcionResponse>(`${API_URL}/roles/${idRol}/permisos/${idOpcion}`, request);
  }

  actualizarPermiso(idRol: number, idOpcion: number, request: RolOpcionRequest): Observable<RolOpcionResponse> {
    return this.http.put<RolOpcionResponse>(`${API_URL}/roles/${idRol}/permisos/${idOpcion}`, request);
  }

  quitarPermiso(idRol: number, idOpcion: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/roles/${idRol}/permisos/${idOpcion}`);
  }

  // --- Usuarios ---

  listarUsuarios(username: string, page: number, size: number): Observable<Page<UsuarioResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'username');
    if (username) {
      params = params.set('username', username);
    }
    return this.http.get<Page<UsuarioResponse>>(`${API_URL}/usuarios`, { params });
  }

  obtenerUsuario(id: number): Observable<UsuarioResponse> {
    return this.http.get<UsuarioResponse>(`${API_URL}/usuarios/${id}`);
  }

  crearUsuario(request: UsuarioRequest): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(`${API_URL}/usuarios`, request);
  }

  actualizarUsuario(id: number, request: UsuarioActualizarRequest): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(`${API_URL}/usuarios/${id}`, request);
  }

  eliminarUsuario(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/usuarios/${id}`);
  }

  cambiarPassword(id: number, password: string): Observable<void> {
    return this.http.put<void>(`${API_URL}/usuarios/${id}/password`, { password });
  }

  desbloquearUsuario(id: number): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(`${API_URL}/usuarios/${id}/desbloquear`, null);
  }
}
