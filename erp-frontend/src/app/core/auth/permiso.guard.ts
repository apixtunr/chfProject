import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TipoPermiso } from '../models/auth';
import { AuthService } from './auth.service';

/**
 * Deja entrar si el rol puede VER todas las paginas indicadas.
 *
 * Casi siempre es una sola: permisoGuard('/api/clientes'). Se admiten varias porque hay
 * pantallas que combinan datos de mas de un modulo y no sirve de nada dejar entrar a
 * quien va a recibir un rechazo del servidor en la mitad de las consultas. El reporte de
 * eventos es el caso: muestra eventos, pero tambien rentabilidad y nombres de clientes.
 *
 * Esto es comodidad, no seguridad: el guardia corre en el navegador y ahi no se protege
 * nada. Quien decide de verdad es el backend, en AutorizacionApi.
 */
export function permisoGuard(...paginasUrl: string[]): CanActivateFn {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (paginasUrl.every((pagina) => authService.puedeVer(pagina))) {
      return true;
    }
    // No redirigir a '/' (redirige a la primera pantalla del menu): si el usuario
    // tampoco tiene permiso ahi, se forma un loop infinito de redirects.
    return router.parseUrl('/sin-acceso');
  };
}

/**
 * Deja entrar si el rol tiene esa accion concreta sobre la pagina.
 *
 * Para los formularios de alta y edicion. Ver un modulo y poder modificarlo son cosas
 * distintas: Cocina puede mirar los eventos pero no crearlos, y con el guardia de ver
 * alcanzaba para abrir el formulario de evento nuevo, llenarlo y recien al guardar
 * enterarse de que no tenia permiso.
 */
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
