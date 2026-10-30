import { inject } from '@angular/core';
import { CanActivateChildFn } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from './auth.service';

export const permisosAlDiaGuard: CanActivateChildFn = () =>
  inject(AuthService)
    .refrescarPermisos()
    .pipe(map(() => true));
