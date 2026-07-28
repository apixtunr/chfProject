import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/** Factory: uso en rutas como canActivate: [permisoGuard('/api/clientes')]. */
export function permisoGuard(paginaUrl: string): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.puedeVer(paginaUrl)) {
      return true;
    }
    // No redirigir a '/' (redirige a la primera pantalla del menu): si el usuario
    // tampoco tiene permiso ahi, se forma un loop infinito de redirects.
    return router.parseUrl('/sin-acceso');
  };
}
