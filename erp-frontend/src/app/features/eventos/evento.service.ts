import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import { CotizacionResponse } from '../cotizaciones/dto/cotizacion';
import {
  CostoEventoRequest,
  CostoEventoResponse,
  DetalleEventoRequest,
  DetalleEventoResponse,
  EventoEmpleadoRequest,
  EventoEmpleadoResponse,
  EventoInventarioRequest,
  EventoInventarioResponse,
  EventoRequest,
  EventoResponse,
  EventoResumenResponse,
  EventoVehiculoRequest,
  EventoVehiculoResponse,
  FiltrosEvento,
  TipoEventoResponse,
} from './dto/evento';

const BASE_URL = `${API_URL}/eventos`;

@Injectable({ providedIn: 'root' })
export class EventoService {
  constructor(private readonly http: HttpClient) {}

  // --- Cabecera ---

  listar(filtros: FiltrosEvento, page: number, size: number, orden = 'fechaEvento,desc'): Observable<Page<EventoResponse>> {
    const params = this.aParams(filtros).set('page', page).set('size', size).set('sort', orden);
    return this.http.get<Page<EventoResponse>>(BASE_URL, { params });
  }

  /** Conteos por estado y por tipo de evento, para el dashboard. */
  resumen(filtros: FiltrosEvento): Observable<EventoResumenResponse> {
    return this.http.get<EventoResumenResponse>(`${BASE_URL}/resumen`, { params: this.aParams(filtros) });
  }

  private aParams(filtros: FiltrosEvento): HttpParams {
    let params = new HttpParams();
    if (filtros.fechaDesde) {
      params = params.set('fechaDesde', filtros.fechaDesde);
    }
    if (filtros.fechaHasta) {
      params = params.set('fechaHasta', filtros.fechaHasta);
    }
    if (filtros.idCliente) {
      params = params.set('idCliente', filtros.idCliente);
    }
    if (filtros.idTipoEvento) {
      params = params.set('idTipoEvento', filtros.idTipoEvento);
    }
    if (filtros.idEstado) {
      params = params.set('idEstado', filtros.idEstado);
    }
    return params;
  }

  obtener(id: number): Observable<EventoResponse> {
    return this.http.get<EventoResponse>(`${BASE_URL}/${id}`);
  }

  crear(request: EventoRequest): Observable<EventoResponse> {
    return this.http.post<EventoResponse>(BASE_URL, request);
  }

  // Servicio que se utilizara a futuro para poder editar, guardar, consultar, eliminar x funcionalidad
  actualizar(id: number, request: EventoRequest): Observable<EventoResponse> {
    return this.http.put<EventoResponse>(`${BASE_URL}/${id}`, request);
  }

  cambiarEstado(id: number, idEstado: number): Observable<EventoResponse> {
    return this.http.put<EventoResponse>(`${BASE_URL}/${id}/estado`, { idEstado });
  }

  listarTiposEvento(): Observable<TipoEventoResponse[]> {
    return this.http.get<TipoEventoResponse[]>(`${API_URL}/tipos-evento`);
  }

  /** Cotizaciones ACEPTADA que todavia no tienen un evento asociado. */
  listarCotizacionesDisponibles(): Observable<CotizacionResponse[]> {
    return this.http.get<CotizacionResponse[]>(`${BASE_URL}/cotizaciones-disponibles`);
  }

  // --- Detalle (menu de eventos directos, sin cotizacion) ---

  listarDetalle(idEvento: number): Observable<DetalleEventoResponse[]> {
    return this.http.get<DetalleEventoResponse[]>(`${BASE_URL}/${idEvento}/detalles`);
  }

  agregarDetalle(idEvento: number, request: DetalleEventoRequest): Observable<DetalleEventoResponse> {
    return this.http.post<DetalleEventoResponse>(`${BASE_URL}/${idEvento}/detalles`, request);
  }

  // Servicio que se utilizara a futuro para poder editar, guardar, consultar, eliminar x funcionalidad
  actualizarDetalle(idEvento: number, idDetalle: number, request: DetalleEventoRequest): Observable<DetalleEventoResponse> {
    return this.http.put<DetalleEventoResponse>(`${BASE_URL}/${idEvento}/detalles/${idDetalle}`, request);
  }

  eliminarDetalle(idEvento: number, idDetalle: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${idEvento}/detalles/${idDetalle}`);
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
