import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../core/api-config';
import { PanelInicio } from './dto/inicio';

@Injectable({ providedIn: 'root' })
export class InicioService {
  private readonly http = inject(HttpClient);

  panel(): Observable<PanelInicio> {
    return this.http.get<PanelInicio>(`${API_URL}/inicio`);
  }
}
