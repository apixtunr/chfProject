import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { API_URL } from '../api-config';
import { SIN_MENSAJE_DE_ERROR } from '../http/sin-mensaje-de-error';
import { LoginRequest, LoginResponse, PermisoResponse, TipoPermiso } from '../models/auth';

const STORAGE_KEY = 'erp-sesion';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly sesion = signal<LoginResponse | null>(this.leerSesionGuardada());

  readonly usuarioActual = computed(() => this.sesion());
  readonly permisos = computed(() => this.sesion()?.permisos ?? []);
  readonly estaAutenticado = computed(() => this.sesion() !== null);
  /**
   * Solo para reglas de negocio propias del rol (cancelar un evento). Los permisos de
   * pantalla del ADMINISTRADOR salen de rol_opcion, igual que los de cualquier rol.
   */
  readonly esAdministrador = computed(() => this.sesion()?.rol === 'ADMINISTRADOR');

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {
    // Sincronizacion entre pestanas: si la sesion se cierra en otra pestana
    // (localStorage cambia a null), esta pestana tambien queda fuera.
    window.addEventListener('storage', (evento) => {
      if (evento.key === STORAGE_KEY && evento.newValue === null && this.sesion() !== null) {
        this.sesion.set(null);
        this.router.navigateByUrl('/login');
      }
    });
    // Al volver a la pestana se traen los permisos vigentes, por si los cambiaron mientras
    // tanto en Roles y permisos.
    window.addEventListener('focus', () => {
      if (this.sesion() !== null) {
        this.refrescarPermisos().subscribe();
      }
    });
  }

  /**
   * Vuelve a pedir al servidor los permisos del rol, para que un cambio hecho en Roles y
   * permisos se refleje sin cerrar sesion. Se llama antes de cada navegacion (guardia
   * permisosAlDiaGuard) y al volver a la pestana. Si la consulta falla se siguen usando
   * los permisos que ya se tenian: el servidor valida igual cada operacion.
   */
  refrescarPermisos(): Observable<void> {
    if (this.sesion() === null) {
      return of(undefined);
    }
    const context = new HttpContext().set(SIN_MENSAJE_DE_ERROR, true);
    return this.http.get<PermisoResponse[]>(`${API_URL}/menu/permisos`, { context }).pipe(
      tap((permisos) => {
        const actual = this.sesion();
        if (actual === null || JSON.stringify(actual.permisos) === JSON.stringify(permisos)) {
          return;
        }
        const nueva = { ...actual, permisos };
        localStorage.setItem(STORAGE_KEY, JSON.stringify(nueva));
        this.sesion.set(nueva);
      }),
      map(() => undefined),
      catchError(() => of(undefined)),
    );
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_URL}/auth/login`, request).pipe(
      tap((respuesta) => {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(respuesta));
        this.sesion.set(respuesta);
      }),
    );
  }

  /**
   * Cierra sesion en el SERVIDOR (revoca los tokens vigentes) y luego limpia
   * el estado local. La limpieza local ocurre aunque el servidor falle o no
   * responda: el usuario nunca debe quedar "atrapado" en la sesion.
   */
  logout(): void {
    const token = this.getToken();
    if (token) {
      this.http.post(`${API_URL}/auth/logout`, null).subscribe({
        next: () => this.limpiarSesionLocal(),
        error: () => this.limpiarSesionLocal(),
      });
    } else {
      this.limpiarSesionLocal();
    }
  }

  private limpiarSesionLocal(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.sesion.set(null);
    this.router.navigateByUrl('/login');
  }

  getToken(): string | null {
    return this.sesion()?.token ?? null;
  }

  /** paginaUrl debe ser el mismo string exacto que manda el backend (ej. '/api/clientes'). */
  tienePermiso(paginaUrl: string, tipo: TipoPermiso): boolean {
    const permiso = this.permisos().find((p) => p.paginaUrl === paginaUrl);
    return permiso ? permiso[tipo] : false;
  }

  /** Puede ver/navegar a la pantalla de ese modulo (no implica poder crear/editar/borrar). */
  puedeVer(paginaUrl: string): boolean {
    return this.permisos().some((p) => p.paginaUrl === paginaUrl);
  }

  private leerSesionGuardada(): LoginResponse | null {
    const crudo = localStorage.getItem(STORAGE_KEY);
    if (!crudo) {
      return null;
    }
    try {
      return JSON.parse(crudo) as LoginResponse;
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }
}
