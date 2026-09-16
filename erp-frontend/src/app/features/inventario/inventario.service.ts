import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import {
  CategoriaProductoResponse,
  InventarioResponse,
  MovimientoInventarioRequest,
  MovimientoInventarioResponse,
  ProductoRequest,
  ProductoResponse,
} from './dto/inventario';

@Injectable({ providedIn: 'root' })
export class InventarioService {
  constructor(private readonly http: HttpClient) {}

  // --- Productos ---

  listarProductos(nombre: string, page: number, size: number): Observable<Page<ProductoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'nombreProducto');
    if (nombre) {
      params = params.set('nombre', nombre);
    }
    return this.http.get<Page<ProductoResponse>>(`${API_URL}/productos`, { params });
  }

  obtenerProducto(id: number): Observable<ProductoResponse> {
    return this.http.get<ProductoResponse>(`${API_URL}/productos/${id}`);
  }

  crearProducto(request: ProductoRequest): Observable<ProductoResponse> {
    return this.http.post<ProductoResponse>(`${API_URL}/productos`, request);
  }

  actualizarProducto(id: number, request: ProductoRequest): Observable<ProductoResponse> {
    return this.http.put<ProductoResponse>(`${API_URL}/productos/${id}`, request);
  }

  eliminarProducto(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/productos/${id}`);
  }

  listarCategorias(): Observable<CategoriaProductoResponse[]> {
    return this.http.get<CategoriaProductoResponse[]>(`${API_URL}/categorias-producto`);
  }

  // --- Stock ---

  listarInventarios(page: number, size: number): Observable<Page<InventarioResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<InventarioResponse>>(`${API_URL}/inventarios`, { params });
  }

  alertasBajoStock(): Observable<InventarioResponse[]> {
    return this.http.get<InventarioResponse[]>(`${API_URL}/inventarios/alertas`);
  }

  actualizarMinimo(idProducto: number, cantidadMinima: number): Observable<InventarioResponse> {
    return this.http.put<InventarioResponse>(`${API_URL}/inventarios/producto/${idProducto}/minimo`, {
      cantidadMinima,
    });
  }

  // --- Movimientos ---

  listarMovimientos(
    idProducto: number | null,
    idEvento: number | null,
    page: number,
    size: number,
  ): Observable<Page<MovimientoInventarioResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'fechaMovimiento,desc');
    if (idProducto) {
      params = params.set('idProducto', idProducto);
    }
    if (idEvento) {
      params = params.set('idEvento', idEvento);
    }
    return this.http.get<Page<MovimientoInventarioResponse>>(`${API_URL}/movimientos-inventario`, { params });
  }

  registrarMovimiento(request: MovimientoInventarioRequest): Observable<MovimientoInventarioResponse> {
    return this.http.post<MovimientoInventarioResponse>(`${API_URL}/movimientos-inventario`, request);
  }
}
