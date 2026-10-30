import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { API_URL } from '../api-config';
import { MODULOS, MODULO_SIN_PRESENTACION, PANTALLAS, VISTAS_AGRUPADAS } from './menu-presentacion';

interface MenuModuloResponse {
  modulo: string;
  opciones: { nombre: string; paginaUrl: string; vista: string }[];
}

export interface MenuPantalla {
  nombre: string;
  ruta: string;
  icono: string;
}

export interface MenuModulo {
  modulo: string;
  icono: string;
  color: string;
  pantallas: MenuPantalla[];
}

@Injectable({ providedIn: 'root' })
export class MenuService {
  private readonly http = inject(HttpClient);

  obtener(): Observable<MenuModulo[]> {
    return this.http.get<MenuModuloResponse[]>(`${API_URL}/menu`).pipe(
      map((modulos) =>
        modulos
          .map((m) => ({ modulo: m.modulo, ...(MODULOS[m.modulo] ?? MODULO_SIN_PRESENTACION), pantallas: pantallasDe(m) }))
          .filter((m) => m.pantallas.length > 0),
      ),
    );
  }
}

function pantallasDe(modulo: MenuModuloResponse): MenuPantalla[] {
  const pantallas: MenuPantalla[] = [];
  const vistasAgregadas = new Set<string>();
  for (const opcion of modulo.opciones) {
    const agrupada = VISTAS_AGRUPADAS[opcion.vista];
    if (agrupada) {
      if (!vistasAgregadas.has(opcion.vista)) {
        vistasAgregadas.add(opcion.vista);
        pantallas.push({ nombre: opcion.vista, ...agrupada });
      }
    } else if (PANTALLAS[opcion.paginaUrl]) {
      pantallas.push({ nombre: opcion.nombre, ...PANTALLAS[opcion.paginaUrl] });
    }
  }
  return pantallas;
}
