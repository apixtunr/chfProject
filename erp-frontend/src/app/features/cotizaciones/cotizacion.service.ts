import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import {
  CotizacionRequest,
  CotizacionResponse,
  CotizacionVersionResponse,
  DetalleCotizacionRequest,
  DetalleCotizacionResponse,
  ServicioCotizacionRequest,
  ServicioCotizacionResponse,
} from './dto/cotizacion';

const BASE_URL = `${API_URL}/cotizaciones`;
const VERSIONES_URL = `${BASE_URL}/versiones`;

@Injectable({ providedIn: 'root' })
export class CotizacionService {
  constructor(private readonly http: HttpClient) {}

  // --- Cabecera ---

  listar(idCliente: number | null, page: number, size: number): Observable<Page<CotizacionResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'fechaCotizacion,desc');
    if (idCliente) {
      params = params.set('idCliente', idCliente);
    }
    return this.http.get<Page<CotizacionResponse>>(BASE_URL, { params });
  }

  obtener(id: number): Observable<CotizacionResponse> {
    return this.http.get<CotizacionResponse>(`${BASE_URL}/${id}`);
  }

  crear(request: CotizacionRequest): Observable<CotizacionResponse> {
    return this.http.post<CotizacionResponse>(BASE_URL, request);
  }

  actualizar(id: number, request: CotizacionRequest): Observable<CotizacionResponse> {
    return this.http.put<CotizacionResponse>(`${BASE_URL}/${id}`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${id}`);
  }

  // --- Versiones ---

  listarVersiones(idCotizacion: number): Observable<CotizacionVersionResponse[]> {
    return this.http.get<CotizacionVersionResponse[]>(`${BASE_URL}/${idCotizacion}/versiones`);
  }

  obtenerVersion(idVersion: number): Observable<CotizacionVersionResponse> {
    return this.http.get<CotizacionVersionResponse>(`${VERSIONES_URL}/${idVersion}`);
  }

  crearVersion(idCotizacion: number, copiarUltimoDetalle: boolean): Observable<CotizacionVersionResponse> {
    const params = new HttpParams().set('copiarUltimoDetalle', copiarUltimoDetalle);
    return this.http.post<CotizacionVersionResponse>(`${BASE_URL}/${idCotizacion}/versiones`, null, { params });
  }

  cambiarEstadoVersion(idVersion: number, idEstado: number): Observable<CotizacionVersionResponse> {
    return this.http.put<CotizacionVersionResponse>(`${VERSIONES_URL}/${idVersion}/estado`, { idEstado });
  }

  descargarPdf(idVersion: number): Observable<Blob> {
    return this.http.get(`${VERSIONES_URL}/${idVersion}/pdf`, { responseType: 'blob' });
  }

  // --- Detalle ---

  listarDetalle(idVersion: number): Observable<DetalleCotizacionResponse[]> {
    return this.http.get<DetalleCotizacionResponse[]>(`${VERSIONES_URL}/${idVersion}/detalles`);
  }

  agregarDetalle(idVersion: number, request: DetalleCotizacionRequest): Observable<DetalleCotizacionResponse> {
    return this.http.post<DetalleCotizacionResponse>(`${VERSIONES_URL}/${idVersion}/detalles`, request);
  }

  actualizarDetalle(
    idVersion: number,
    idDetalle: number,
    request: DetalleCotizacionRequest,
  ): Observable<DetalleCotizacionResponse> {
    return this.http.put<DetalleCotizacionResponse>(`${VERSIONES_URL}/${idVersion}/detalles/${idDetalle}`, request);
  }

  eliminarDetalle(idVersion: number, idDetalle: number): Observable<void> {
    return this.http.delete<void>(`${VERSIONES_URL}/${idVersion}/detalles/${idDetalle}`);
  }

  // --- Servicios extra (bebidas, decoracion, personal, etc.) ---

  listarServicios(idVersion: number): Observable<ServicioCotizacionResponse[]> {
    return this.http.get<ServicioCotizacionResponse[]>(`${VERSIONES_URL}/${idVersion}/servicios`);
  }

  agregarServicio(idVersion: number, request: ServicioCotizacionRequest): Observable<ServicioCotizacionResponse> {
    return this.http.post<ServicioCotizacionResponse>(`${VERSIONES_URL}/${idVersion}/servicios`, request);
  }

  actualizarServicio(
    idVersion: number,
    idServicio: number,
    request: ServicioCotizacionRequest,
  ): Observable<ServicioCotizacionResponse> {
    return this.http.put<ServicioCotizacionResponse>(`${VERSIONES_URL}/${idVersion}/servicios/${idServicio}`, request);
  }

  eliminarServicio(idVersion: number, idServicio: number): Observable<void> {
    return this.http.delete<void>(`${VERSIONES_URL}/${idVersion}/servicios/${idServicio}`);
  }
}
