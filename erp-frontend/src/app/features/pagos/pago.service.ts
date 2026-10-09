import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import {
  ComprobantePagoRequest,
  ComprobantePagoResponse,
  EventoPagoResponse,
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

  /** Eventos con su saldo (total/abonado/pendiente). filtro: PENDIENTE (default) o PAGADO. */
  listarEventosConSaldo(filtro: string | null, page: number, size: number): Observable<Page<EventoPagoResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'fechaEvento,desc');
    if (filtro) {
      params = params.set('filtro', filtro);
    }
    return this.http.get<Page<EventoPagoResponse>>(`${BASE_URL}/eventos`, { params });
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

  /** Recibo del pago en PDF (se pide con el token, como cualquier llamada). */
  recibo(idPago: number): Observable<Blob> {
    return this.http.get(`${BASE_URL}/${idPago}/recibo`, { responseType: 'blob' });
  }

  // --- Comprobantes ---

  listarComprobantes(idPago: number): Observable<ComprobantePagoResponse[]> {
    return this.http.get<ComprobantePagoResponse[]>(`${BASE_URL}/${idPago}/comprobantes`);
  }

  /** Datos y archivo en una sola peticion: si el archivo no sirve, no queda el comprobante a medias. */
  agregarComprobante(
    idPago: number,
    request: ComprobantePagoRequest,
    archivo: File | null,
  ): Observable<ComprobantePagoResponse> {
    const datos = new FormData();
    datos.append('datos', new Blob([JSON.stringify(request)], { type: 'application/json' }));
    if (archivo) {
      datos.append('archivo', archivo, archivo.name);
    }
    return this.http.post<ComprobantePagoResponse>(`${BASE_URL}/${idPago}/comprobantes`, datos);
  }

  actualizarComprobante(
    idPago: number,
    idComprobante: number,
    request: ComprobantePagoRequest,
  ): Observable<ComprobantePagoResponse> {
    return this.http.put<ComprobantePagoResponse>(`${BASE_URL}/${idPago}/comprobantes/${idComprobante}`, request);
  }

  /** Adjunta el archivo a un comprobante que no lo tenia, o lo reemplaza. */
  guardarArchivoComprobante(idPago: number, idComprobante: number, archivo: File): Observable<ComprobantePagoResponse> {
    const datos = new FormData();
    datos.append('archivo', archivo, archivo.name);
    return this.http.put<ComprobantePagoResponse>(`${BASE_URL}/${idPago}/comprobantes/${idComprobante}/archivo`, datos);
  }

  /** El archivo se pide con el token como cualquier llamada: no hay enlace publico. */
  archivoComprobante(idPago: number, idComprobante: number): Observable<Blob> {
    return this.http.get(`${BASE_URL}/${idPago}/comprobantes/${idComprobante}/archivo`, { responseType: 'blob' });
  }

  eliminarComprobante(idPago: number, idComprobante: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/${idPago}/comprobantes/${idComprobante}`);
  }
}
