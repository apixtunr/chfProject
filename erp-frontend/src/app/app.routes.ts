import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { Shell } from './core/layout/shell';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then((m) => m.Login),
  },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        loadComponent: () => import('./features/home/home').then((m) => m.Home),
      },
      {
        path: 'clientes',
        loadComponent: () => import('./features/clientes/cliente-list/cliente-list').then((m) => m.ClienteList),
      },
      {
        path: 'clientes/nuevo',
        loadComponent: () => import('./features/clientes/cliente-form/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'clientes/:id/editar',
        loadComponent: () => import('./features/clientes/cliente-form/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'cotizaciones',
        loadComponent: () =>
          import('./features/cotizaciones/cotizacion-list/cotizacion-list').then((m) => m.CotizacionList),
      },
      {
        path: 'cotizaciones/nueva',
        loadComponent: () =>
          import('./features/cotizaciones/cotizacion-form/cotizacion-form').then((m) => m.CotizacionForm),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
