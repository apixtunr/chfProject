import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import {
  MenuPlatoRequest,
  MenuPlatoResponse,
  MenuRequest,
  MenuResponse,
  PlatoRequest,
  PlatoResponse,
} from './dto/menu';

@Injectable({ providedIn: 'root' })
export class MenuPlatoService {
  constructor(private readonly http: HttpClient) {}

  // --- Platos ---

  listarPlatos(nombre: string, page: number, size: number): Observable<Page<PlatoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'nombrePlato');
    if (nombre) {
      params = params.set('nombre', nombre);
    }
    return this.http.get<Page<PlatoResponse>>(`${API_URL}/platos`, { params });
  }

  obtenerPlato(id: number): Observable<PlatoResponse> {
    return this.http.get<PlatoResponse>(`${API_URL}/platos/${id}`);
  }

  crearPlato(request: PlatoRequest): Observable<PlatoResponse> {
    return this.http.post<PlatoResponse>(`${API_URL}/platos`, request);
  }

  actualizarPlato(id: number, request: PlatoRequest): Observable<PlatoResponse> {
    return this.http.put<PlatoResponse>(`${API_URL}/platos/${id}`, request);
  }

  eliminarPlato(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/platos/${id}`);
  }

  // --- Menus ---

  listarMenus(nombre: string, page: number, size: number): Observable<Page<MenuResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'nombreMenu');
    if (nombre) {
      params = params.set('nombre', nombre);
    }
    return this.http.get<Page<MenuResponse>>(`${API_URL}/menus`, { params });
  }

  obtenerMenu(id: number): Observable<MenuResponse> {
    return this.http.get<MenuResponse>(`${API_URL}/menus/${id}`);
  }

  crearMenu(request: MenuRequest): Observable<MenuResponse> {
    return this.http.post<MenuResponse>(`${API_URL}/menus`, request);
  }

  actualizarMenu(id: number, request: MenuRequest): Observable<MenuResponse> {
    return this.http.put<MenuResponse>(`${API_URL}/menus/${id}`, request);
  }

  eliminarMenu(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/menus/${id}`);
  }

  // --- Composicion del menu (menu_plato) ---

  listarPlatosDeMenu(idMenu: number): Observable<MenuPlatoResponse[]> {
    return this.http.get<MenuPlatoResponse[]>(`${API_URL}/menus/${idMenu}/platos`);
  }

  agregarPlatoAMenu(idMenu: number, idPlato: number, request: MenuPlatoRequest): Observable<MenuPlatoResponse> {
    return this.http.post<MenuPlatoResponse>(`${API_URL}/menus/${idMenu}/platos/${idPlato}`, request);
  }

  actualizarPlatoDeMenu(idMenu: number, idPlato: number, request: MenuPlatoRequest): Observable<MenuPlatoResponse> {
    return this.http.put<MenuPlatoResponse>(`${API_URL}/menus/${idMenu}/platos/${idPlato}`, request);
  }

  quitarPlatoDeMenu(idMenu: number, idPlato: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/menus/${idMenu}/platos/${idPlato}`);
  }
}
