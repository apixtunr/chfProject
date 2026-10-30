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
  readonly esAdministrador = computed(() => this.sesion()?.rol === 'ADMINISTRADOR');

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {
    window.addEventListener('storage', (evento) => {
      if (evento.key === STORAGE_KEY && evento.newValue === null && this.sesion() !== null) {
        this.sesion.set(null);
        this.router.navigateByUrl('/login');
      }
    });
    window.addEventListener('focus', () => {
      if (this.sesion() !== null) {
        this.refrescarPermisos().subscribe();
      }
    });
  }

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

  tienePermiso(paginaUrl: string, tipo: TipoPermiso): boolean {
    const permiso = this.permisos().find((p) => p.paginaUrl === paginaUrl);
    return permiso ? permiso[tipo] : false;
  }

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
