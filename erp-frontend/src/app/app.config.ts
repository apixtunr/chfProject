import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { MAT_DATE_LOCALE, provideNativeDateAdapter } from '@angular/material/core';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { jwtInterceptor } from './core/http/jwt.interceptor';
import { errorInterceptor } from './core/http/error.interceptor';
import { crearPaginatorIntlEspanol } from './core/i18n/spanish-paginator-intl';
import { FORMATO_FECHA_ES } from './core/i18n/spanish-date-formats';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor])),
    provideNativeDateAdapter(FORMATO_FECHA_ES),
    { provide: MAT_DATE_LOCALE, useValue: 'es-GT' },
    { provide: MatPaginatorIntl, useValue: crearPaginatorIntlEspanol() }
  ]
};
