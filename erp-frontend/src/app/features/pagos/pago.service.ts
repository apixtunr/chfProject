import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import {
  ComprobantePagoRequest,
  ComprobantePagoResponse,
  MetodoPagoResponse,
  PagoRequest,
  PagoResponse,
} from './dto/pago';

const BASE_URL = `${API_URL}/pagos`;

@Injectable({ providedIn: 'root' })
export class PagoService {
  constructor(private readonly http: HttpClient) {}

  listar(idEvento: number | null, page: number, size: number): Observable<Page<PagoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'fechaPago,desc');
    if (idEvento) {
      params = params.set('idEvento', idEvento);
    }
    return this.http.get<Page<PagoResponse>>(BASE_URL, { params });
  }

  obtener(id: number): Observable<PagoResponse> {
    return this.http.get<PagoResponse>(`${BASE_URL}/${id}`);
  }

  crear(request: PagoRequest): Observable<PagoResponse> {
    return this.http.post<PagoResponse>(BASE_URL, request);
  }

  actualizar(id: number, request: PagoRequest): Observable<PagoResponse> {
    return this.http.put<PagoResponse>(`${BASE_URL}/${id}`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${id}`);
  }

  cambiarEstado(id: number, idEstado: number): Observable<PagoResponse> {
    return this.http.put<PagoResponse>(`${BASE_URL}/${id}/estado`, { idEstado });
  }

  listarMetodos(): Observable<MetodoPagoResponse[]> {
    return this.http.get<MetodoPagoResponse[]>(`${API_URL}/metodos-pago`);
  }

  // --- Comprobantes ---

  listarComprobantes(idPago: number): Observable<ComprobantePagoResponse[]> {
    return this.http.get<ComprobantePagoResponse[]>(`${BASE_URL}/${idPago}/comprobantes`);
  }

  agregarComprobante(idPago: number, request: ComprobantePagoRequest): Observable<ComprobantePagoResponse> {
    return this.http.post<ComprobantePagoResponse>(`${BASE_URL}/${idPago}/comprobantes`, request);
  }

  eliminarComprobante(idPago: number, idComprobante: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${idPago}/comprobantes/${idComprobante}`);
  }
}
