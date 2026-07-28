import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { ApiError } from '../models/api-error';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 401 && !req.url.includes('/auth/login')) {
          authService.logout();
          router.navigateByUrl('/login');
        } else {
          const apiError = error.error as ApiError | undefined;
          const mensaje = apiError?.mensaje ?? 'Ocurrio un error inesperado';
          snackBar.open(mensaje, 'Cerrar', { duration: 5000 });
        }
      }
      return throwError(() => error);
    }),
  );
};
