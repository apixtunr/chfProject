import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { API_URL } from '../api-config';
import { Page } from '../models/page';
import { MenuResponse } from './menu';

/** Catalogo de menus activos, usado para armar el detalle de una cotizacion. */
@Injectable({ providedIn: 'root' })
export class MenuService {
  constructor(private readonly http: HttpClient) {}

  listarActivos(): Observable<MenuResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 200).set('sort', 'nombreMenu');
    return this.http
      .get<Page<MenuResponse>>(`${API_URL}/menus`, { params })
      .pipe(map((pagina) => pagina.content.filter((m) => m.estadoNombre === 'ACTIVO')));
  }
}
