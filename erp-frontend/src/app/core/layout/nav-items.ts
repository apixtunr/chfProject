export interface NavItem {
  label: string;
  route: string;
  /** Debe matchear el paginaUrl que devuelve el backend para esa opcion. */
  paginaUrl: string;
  icon: string;
}

export interface NavGroup {
  /** Debe matchear el nombre del modulo en la tabla `modulo` (seeds V3). */
  modulo: string;
  icon: string;
  /** Color de la pastilla del icono en el menu (identidad visual por modulo). */
  color: string;
  items: NavItem[];
}

/**
 * Menu lateral agrupado en los 8 modulos del alcance del sistema (mismos que
 * la tabla `modulo`). Cada item es una opcion (pantalla) y se filtra contra
 * AuthService.puedeVer() antes de mostrarse; los modulos sin items visibles
 * se ocultan completos.
 */
export const NAV_GROUPS: NavGroup[] = [
  {
    modulo: 'Clientes',
    color: '#3F51B5',
    icon: 'people',
    items: [{ label: 'Clientes', route: '/clientes', paginaUrl: '/api/clientes', icon: 'people' }],
  },
  {
    modulo: 'Cotizaciones',
    color: '#8E44AD',
    icon: 'request_quote',
    items: [{ label: 'Cotizaciones', route: '/cotizaciones', paginaUrl: '/api/cotizaciones', icon: 'request_quote' }],
  },
  {
    modulo: 'Eventos',
    color: '#00ACC1',
    icon: 'event',
    items: [
      { label: 'Gestión Eventos', route: '/eventos', paginaUrl: '/api/eventos', icon: 'event' },
      { label: 'Reporte Eventos', route: '/eventos/reporte', paginaUrl: '/api/eventos', icon: 'insights' },
    ],
  },
  {
    modulo: 'Inventarios',
    color: '#F39C12',
    icon: 'inventory_2',
    items: [
      { label: 'Stock', route: '/inventario', paginaUrl: '/api/inventarios', icon: 'inventory_2' },
      { label: 'Productos', route: '/inventario/productos', paginaUrl: '/api/productos', icon: 'category' },
      { label: 'Movimientos', route: '/inventario/movimientos', paginaUrl: '/api/movimientos-inventario', icon: 'swap_vert' },
    ],
  },
  {
    modulo: 'Menús y platos',
    color: '#27AE60',
    icon: 'restaurant_menu',
    items: [
      { label: 'Menús', route: '/menus', paginaUrl: '/api/menus', icon: 'restaurant_menu' },
      { label: 'Platos', route: '/menus/platos', paginaUrl: '/api/platos', icon: 'restaurant' },
    ],
  },
  {
    modulo: 'Pagos',
    color: '#16A085',
    icon: 'payments',
    items: [{ label: 'Pagos', route: '/pagos', paginaUrl: '/api/pagos', icon: 'payments' }],
  },
  {
    modulo: 'Rentabilidad',
    color: '#D35400',
    icon: 'trending_up',
    items: [{ label: 'Reporte', route: '/rentabilidad', paginaUrl: '/api/rentabilidad', icon: 'trending_up' }],
  },
  {
    modulo: 'Administracion',
    color: '#E74C3C',
    icon: 'settings',
    items: [
      { label: 'Usuarios', route: '/admin/usuarios', paginaUrl: '/api/usuarios', icon: 'manage_accounts' },
      { label: 'Roles y permisos', route: '/admin/roles', paginaUrl: '/api/roles', icon: 'admin_panel_settings' },
      { label: 'Empleados', route: '/empleados', paginaUrl: '/api/empleados', icon: 'badge' },
      { label: 'Vehiculos', route: '/vehiculos', paginaUrl: '/api/vehiculos', icon: 'local_shipping' },
      { label: 'Catalogos', route: '/admin/catalogos', paginaUrl: '/api/estados', icon: 'list_alt' },
      { label: 'Bitácora', route: '/admin/bitacora', paginaUrl: '/api/bitacora', icon: 'history' },
    ],
  },
];
