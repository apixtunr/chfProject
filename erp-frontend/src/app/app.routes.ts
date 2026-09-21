import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { permisoGuard } from './core/auth/permiso.guard';
import { catalogoPermisoGuard } from './features/administracion/catalogos/catalogo-permiso.guard';
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
        path: 'sin-acceso',
        loadComponent: () => import('./core/layout/sin-acceso/sin-acceso').then((m) => m.SinAcceso),
      },
      {
        path: 'clientes',
        canActivate: [permisoGuard('/api/clientes')],
        loadComponent: () => import('./features/clientes/cliente-list/cliente-list').then((m) => m.ClienteList),
      },
      {
        path: 'clientes/nuevo',
        canActivate: [permisoGuard('/api/clientes')],
        loadComponent: () => import('./features/clientes/cliente-form/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'clientes/:id/editar',
        canActivate: [permisoGuard('/api/clientes')],
        loadComponent: () => import('./features/clientes/cliente-form/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'cotizaciones',
        canActivate: [permisoGuard('/api/cotizaciones')],
        loadComponent: () =>
          import('./features/cotizaciones/cotizacion-list/cotizacion-list').then((m) => m.CotizacionList),
      },
      {
        path: 'cotizaciones/nueva',
        canActivate: [permisoGuard('/api/cotizaciones')],
        loadComponent: () =>
          import('./features/cotizaciones/cotizacion-form/cotizacion-form').then((m) => m.CotizacionForm),
      },
      {
        path: 'cotizaciones/:id',
        canActivate: [permisoGuard('/api/cotizaciones')],
        loadComponent: () =>
          import('./features/cotizaciones/cotizacion-detail/cotizacion-detail').then((m) => m.CotizacionDetail),
      },
      {
        path: 'cotizaciones/:id/versiones/:versionId',
        canActivate: [permisoGuard('/api/cotizaciones')],
        loadComponent: () =>
          import('./features/cotizaciones/version-detalle/version-detalle').then((m) => m.VersionDetalle),
      },
      {
        path: 'eventos',
        canActivate: [permisoGuard('/api/eventos')],
        loadComponent: () => import('./features/eventos/evento-list/evento-list').then((m) => m.EventoList),
      },
      {
        path: 'eventos/nuevo',
        canActivate: [permisoGuard('/api/eventos')],
        loadComponent: () => import('./features/eventos/evento-form/evento-form').then((m) => m.EventoForm),
      },
      {
        path: 'eventos/reporte',
        canActivate: [permisoGuard('/api/eventos')],
        loadComponent: () => import('./features/eventos/evento-reporte/evento-reporte').then((m) => m.EventoReporte),
      },
      {
        path: 'eventos/:id',
        canActivate: [permisoGuard('/api/eventos')],
        loadComponent: () => import('./features/eventos/evento-detail/evento-detail').then((m) => m.EventoDetail),
      },
      {
        path: 'empleados',
        canActivate: [permisoGuard('/api/empleados')],
        loadComponent: () => import('./features/empleados/empleado-list/empleado-list').then((m) => m.EmpleadoList),
      },
      {
        path: 'empleados/nuevo',
        canActivate: [permisoGuard('/api/empleados')],
        loadComponent: () => import('./features/empleados/empleado-form/empleado-form').then((m) => m.EmpleadoForm),
      },
      {
        path: 'empleados/:id/editar',
        canActivate: [permisoGuard('/api/empleados')],
        loadComponent: () => import('./features/empleados/empleado-form/empleado-form').then((m) => m.EmpleadoForm),
      },
      {
        path: 'vehiculos',
        canActivate: [permisoGuard('/api/vehiculos')],
        loadComponent: () => import('./features/vehiculos/vehiculo-list/vehiculo-list').then((m) => m.VehiculoList),
      },
      {
        path: 'vehiculos/nuevo',
        canActivate: [permisoGuard('/api/vehiculos')],
        loadComponent: () => import('./features/vehiculos/vehiculo-form/vehiculo-form').then((m) => m.VehiculoForm),
      },
      {
        path: 'vehiculos/:id/editar',
        canActivate: [permisoGuard('/api/vehiculos')],
        loadComponent: () => import('./features/vehiculos/vehiculo-form/vehiculo-form').then((m) => m.VehiculoForm),
      },
      {
        path: 'inventario',
        canActivate: [permisoGuard('/api/inventarios')],
        loadComponent: () => import('./features/inventario/stock-list/stock-list').then((m) => m.StockList),
      },
      {
        path: 'inventario/productos',
        canActivate: [permisoGuard('/api/productos')],
        loadComponent: () => import('./features/inventario/producto-list/producto-list').then((m) => m.ProductoList),
      },
      {
        path: 'inventario/productos/nuevo',
        canActivate: [permisoGuard('/api/productos')],
        loadComponent: () => import('./features/inventario/producto-form/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'inventario/productos/:id/editar',
        canActivate: [permisoGuard('/api/productos')],
        loadComponent: () => import('./features/inventario/producto-form/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'inventario/movimientos',
        canActivate: [permisoGuard('/api/movimientos-inventario')],
        loadComponent: () =>
          import('./features/inventario/movimiento-list/movimiento-list').then((m) => m.MovimientoList),
      },
      {
        path: 'inventario/movimientos/nuevo',
        canActivate: [permisoGuard('/api/movimientos-inventario')],
        loadComponent: () =>
          import('./features/inventario/movimiento-form/movimiento-form').then((m) => m.MovimientoForm),
      },
      {
        path: 'pagos',
        canActivate: [permisoGuard('/api/pagos')],
        loadComponent: () => import('./features/pagos/pago-list/pago-list').then((m) => m.PagoList),
      },
      {
        path: 'pagos/evento/:idEvento',
        canActivate: [permisoGuard('/api/pagos')],
        loadComponent: () => import('./features/pagos/pago-evento/pago-evento').then((m) => m.PagoEvento),
      },
      {
        path: 'pagos/evento/:idEvento/nuevo',
        canActivate: [permisoGuard('/api/pagos')],
        loadComponent: () => import('./features/pagos/pago-form/pago-form').then((m) => m.PagoForm),
      },
      {
        path: 'pagos/:id',
        canActivate: [permisoGuard('/api/pagos')],
        loadComponent: () => import('./features/pagos/pago-detail/pago-detail').then((m) => m.PagoDetail),
      },
      {
        path: 'pagos/:id/editar',
        canActivate: [permisoGuard('/api/pagos')],
        loadComponent: () => import('./features/pagos/pago-form/pago-form').then((m) => m.PagoForm),
      },
      // Ojo con el orden: las rutas literales (menus/platos, menus/nuevo)
      // deben ir antes que las parametrizadas (menus/:id)
      {
        path: 'menus/platos',
        canActivate: [permisoGuard('/api/platos')],
        loadComponent: () => import('./features/menus/plato-list/plato-list').then((m) => m.PlatoList),
      },
      {
        path: 'menus/platos/nuevo',
        canActivate: [permisoGuard('/api/platos')],
        loadComponent: () => import('./features/menus/plato-form/plato-form').then((m) => m.PlatoForm),
      },
      {
        path: 'menus/platos/:id/editar',
        canActivate: [permisoGuard('/api/platos')],
        loadComponent: () => import('./features/menus/plato-form/plato-form').then((m) => m.PlatoForm),
      },
      {
        path: 'menus',
        canActivate: [permisoGuard('/api/menus')],
        loadComponent: () => import('./features/menus/menu-list/menu-list').then((m) => m.MenuList),
      },
      {
        path: 'menus/nuevo',
        canActivate: [permisoGuard('/api/menus')],
        loadComponent: () => import('./features/menus/menu-form/menu-form').then((m) => m.MenuForm),
      },
      {
        path: 'menus/:id',
        canActivate: [permisoGuard('/api/menus')],
        loadComponent: () => import('./features/menus/menu-detail/menu-detail').then((m) => m.MenuDetail),
      },
      {
        path: 'menus/:id/editar',
        canActivate: [permisoGuard('/api/menus')],
        loadComponent: () => import('./features/menus/menu-form/menu-form').then((m) => m.MenuForm),
      },
      {
        path: 'admin/catalogos',
        loadComponent: () =>
          import('./features/administracion/catalogos/catalogo-index').then((m) => m.CatalogoIndex),
      },
      {
        path: 'admin/catalogos/:catalogo',
        canActivate: [catalogoPermisoGuard],
        loadComponent: () =>
          import('./features/administracion/catalogos/catalogo-generico').then((m) => m.CatalogoGenerico),
      },
      {
        path: 'admin/roles',
        canActivate: [permisoGuard('/api/roles')],
        loadComponent: () => import('./features/administracion/roles/rol-list').then((m) => m.RolList),
      },
      {
        path: 'admin/roles/:id/permisos',
        canActivate: [permisoGuard('/api/roles')],
        loadComponent: () => import('./features/administracion/roles/rol-permisos').then((m) => m.RolPermisos),
      },
      {
        path: 'admin/usuarios',
        canActivate: [permisoGuard('/api/usuarios')],
        loadComponent: () => import('./features/administracion/usuarios/usuario-list').then((m) => m.UsuarioList),
      },
      {
        path: 'admin/usuarios/nuevo',
        canActivate: [permisoGuard('/api/usuarios')],
        loadComponent: () => import('./features/administracion/usuarios/usuario-form').then((m) => m.UsuarioForm),
      },
      {
        path: 'admin/usuarios/:id/editar',
        canActivate: [permisoGuard('/api/usuarios')],
        loadComponent: () => import('./features/administracion/usuarios/usuario-form').then((m) => m.UsuarioForm),
      },
      {
        path: 'rentabilidad',
        canActivate: [permisoGuard('/api/rentabilidad')],
        loadComponent: () =>
          import('./features/rentabilidad/rentabilidad-report/rentabilidad-report').then((m) => m.RentabilidadReport),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
