import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { Page } from '../../core/models/page';
import { ClienteRequest, ClienteResponse, EstadoClienteFiltro, PosibleDuplicado } from './dto/cliente';

const BASE_URL = `${API_URL}/clientes`;

@Injectable({ providedIn: 'root' })
export class ClienteService {
  constructor(private readonly http: HttpClient) {}

  /**
   * busqueda: texto libre sobre nombre, NIT, telefono y correo.
   * estado: los selectores para crear algo nuevo (cotizacion, evento) piden 'ACTIVO';
   * filtros y reportes de historial usan 'TODOS' (por defecto) para no perder clientes
   * que ya se inactivaron.
   */
  listar(busqueda: string, page: number, size: number, estado: EstadoClienteFiltro = 'TODOS'): Observable<Page<ClienteResponse>> {
    let params = new HttpParams().set('page', page).set('size', size).set('sort', 'nombre').set('estado', estado);
    if (busqueda) {
      params = params.set('buscar', busqueda);
    }
    return this.http.get<Page<ClienteResponse>>(BASE_URL, { params });
  }

  obtener(id: number): Observable<ClienteResponse> {
    return this.http.get<ClienteResponse>(`${BASE_URL}/${id}`);
  }

  crear(request: ClienteRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(BASE_URL, request);
  }

  actualizar(id: number, request: ClienteRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`${BASE_URL}/${id}`, request);
  }

  /** Inactivar (false) o reactivar (true). Los clientes no se borran. */
  cambiarEstado(id: number, activo: boolean): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`${BASE_URL}/${id}/estado`, { activo });
  }

  posiblesDuplicados(datos: { nombre?: string; nit?: string; telefono?: string; correo?: string }, excluir: number | null): Observable<PosibleDuplicado[]> {
    let params = new HttpParams();
    for (const [clave, valor] of Object.entries(datos)) {
      if (valor && valor.trim()) {
        params = params.set(clave, valor.trim());
      }
    }
    if (excluir) {
      params = params.set('excluir', excluir);
    }
    return this.http.get<PosibleDuplicado[]>(`${BASE_URL}/posibles-duplicados`, { params });
  }
}
