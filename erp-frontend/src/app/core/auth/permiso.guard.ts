import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TipoPermiso } from '../models/auth';
import { AuthService } from './auth.service';

export function permisoGuard(...paginasUrl: string[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (paginasUrl.every((pagina) => authService.puedeVer(pagina))) {
      return true;
    }
    return router.parseUrl('/sin-acceso');
  };
}

export function permisoAccionGuard(paginaUrl: string, accion: TipoPermiso): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.tienePermiso(paginaUrl, accion)) {
      return true;
    }
    return router.parseUrl('/sin-acceso');
  };
}
