import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { NAV_GROUPS } from '../layout/nav-items';
import { AuthService } from './auth.service';

/**
 * Redirige la ruta raiz al primer modulo que el usuario realmente puede ver, en vez
 * de a un modulo fijo (ej. 'clientes'): un rol sin permiso ahi quedaba rebotado a
 * /sin-acceso aunque tuviera acceso a otro modulo.
 */
export const inicioGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  for (const grupo of NAV_GROUPS) {
    for (const item of grupo.items) {
      if (authService.puedeVer(item.paginaUrl)) {
        return router.parseUrl(item.route);
      }
    }
  }
  return router.parseUrl('/sin-acceso');
};
