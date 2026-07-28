import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { API_URL } from '../api-config';
import { Page } from '../models/page';
import { ProductoResponse } from './producto';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  constructor(private readonly http: HttpClient) {}

  listarTodos(): Observable<ProductoResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 200).set('sort', 'nombreProducto');
    return this.http
      .get<Page<ProductoResponse>>(`${API_URL}/productos`, { params })
      .pipe(map((pagina) => pagina.content));
  }
}
