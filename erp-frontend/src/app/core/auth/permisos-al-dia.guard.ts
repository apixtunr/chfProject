import { inject } from '@angular/core';
import { CanActivateChildFn } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Antes de abrir cualquier pantalla trae los permisos vigentes del rol. Las pantallas
 * calculan sus botones al abrirse, asi que un permiso dado o quitado en Roles y permisos
 * se ve en la siguiente pantalla que se abra, sin cerrar sesion.
 */
export const permisosAlDiaGuard: CanActivateChildFn = () =>
  inject(AuthService)
    .refrescarPermisos()
    .pipe(map(() => true));
