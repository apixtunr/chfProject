import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { CATALOGOS } from './catalogos-config';

/**
 * Variante de permisoGuard para el catalogo generico: la pagina_url a validar
 * depende del parametro :catalogo de la ruta.
 */
export const catalogoPermisoGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const config = CATALOGOS.find((c) => c.id === route.paramMap.get('catalogo'));
  if (config && authService.puedeVer(config.paginaUrl)) {
    return true;
  }
  return router.parseUrl('/admin/catalogos');
};
