import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import {
  CostoEventoRequest,
  CostoEventoResponse,
  EventoEmpleadoRequest,
  EventoEmpleadoResponse,
  EventoInventarioRequest,
  EventoInventarioResponse,
  EventoRequest,
  EventoResponse,
  EventoVehiculoRequest,
  EventoVehiculoResponse,
  TipoEventoResponse,
} from './dto/evento';

const BASE_URL = `${API_URL}/eventos`;

@Injectable({ providedIn: 'root' })
export class EventoService {
  constructor(private readonly http: HttpClient) {}

  // --- Cabecera ---

  listar(
    fechaDesde: string | null,
    fechaHasta: string | null,
    page: number,
    size: number,
    orden = 'fechaEvento,desc',
  ): Observable<Page<EventoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', orden);
    if (fechaDesde) {
      params = params.set('fechaDesde', fechaDesde);
    }
    if (fechaHasta) {
      params = params.set('fechaHasta', fechaHasta);
    }
    return this.http.get<Page<EventoResponse>>(BASE_URL, { params });
  }

  obtener(id: number): Observable<EventoResponse> {
    return this.http.get<EventoResponse>(`${BASE_URL}/${id}`);
  }

  crear(request: EventoRequest): Observable<EventoResponse> {
    return this.http.post<EventoResponse>(BASE_URL, request);
  }

  actualizar(id: number, request: EventoRequest): Observable<EventoResponse> {
    return this.http.put<EventoResponse>(`${BASE_URL}/${id}`, request);
  }

  cambiarEstado(id: number, idEstado: number): Observable<EventoResponse> {
    return this.http.put<EventoResponse>(`${BASE_URL}/${id}/estado`, { idEstado });
  }

  listarTiposEvento(): Observable<TipoEventoResponse[]> {
    return this.http.get<TipoEventoResponse[]>(`${API_URL}/tipos-evento`);
  }

  // --- Costos ---

  listarCostos(idEvento: number): Observable<CostoEventoResponse[]> {
    return this.http.get<CostoEventoResponse[]>(`${BASE_URL}/${idEvento}/costos`);
  }

  agregarCosto(idEvento: number, request: CostoEventoRequest): Observable<CostoEventoResponse> {
    return this.http.post<CostoEventoResponse>(`${BASE_URL}/${idEvento}/costos`, request);
  }

  eliminarCosto(idEvento: number, idCosto: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${idEvento}/costos/${idCosto}`);
  }

  // --- Personal ---

  listarPersonal(idEvento: number): Observable<EventoEmpleadoResponse[]> {
    return this.http.get<EventoEmpleadoResponse[]>(`${BASE_URL}/${idEvento}/empleados`);
  }

  asignarEmpleado(idEvento: number, idEmpleado: number, request: EventoEmpleadoRequest): Observable<EventoEmpleadoResponse> {
    return this.http.post<EventoEmpleadoResponse>(`${BASE_URL}/${idEvento}/empleados/${idEmpleado}`, request);
  }

  quitarEmpleado(idEvento: number, idEmpleado: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${idEvento}/empleados/${idEmpleado}`);
  }

  // --- Vehiculos ---

  listarVehiculos(idEvento: number): Observable<EventoVehiculoResponse[]> {
    return this.http.get<EventoVehiculoResponse[]>(`${BASE_URL}/${idEvento}/vehiculos`);
  }

  asignarVehiculo(idEvento: number, idVehiculo: number, request: EventoVehiculoRequest): Observable<EventoVehiculoResponse> {
    return this.http.post<EventoVehiculoResponse>(`${BASE_URL}/${idEvento}/vehiculos/${idVehiculo}`, request);
  }

  quitarVehiculo(idEvento: number, idVehiculo: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${idEvento}/vehiculos/${idVehiculo}`);
  }

  // --- Inventario ---

  listarInventario(idEvento: number): Observable<EventoInventarioResponse[]> {
    return this.http.get<EventoInventarioResponse[]>(`${BASE_URL}/${idEvento}/inventario`);
  }

  planificarProducto(idEvento: number, idProducto: number, request: EventoInventarioRequest): Observable<EventoInventarioResponse> {
    return this.http.post<EventoInventarioResponse>(`${BASE_URL}/${idEvento}/inventario/${idProducto}`, request);
  }

  quitarProducto(idEvento: number, idProducto: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${idEvento}/inventario/${idProducto}`);
  }

  confirmarConsumo(idEvento: number, idProducto: number): Observable<EventoInventarioResponse> {
    return this.http.put<EventoInventarioResponse>(`${BASE_URL}/${idEvento}/inventario/${idProducto}/consumir`, null);
  }
}
