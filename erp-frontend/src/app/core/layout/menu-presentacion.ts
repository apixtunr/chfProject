/**
 * Como se dibuja el menu. La base de datos decide QUE se muestra (modulos, opciones, nombres,
 * orden y permisos por rol); aca solo se dice COMO: el icono y el color de cada modulo, y a que
 * pantalla lleva cada opcion, buscada por su pagina_url.
 *
 * Una opcion de la base que no aparece aca es un permiso que no es pantalla (ubicaciones,
 * metodos de pago, opciones del sistema) y no sale en el menu.
 */

export interface PresentacionModulo {
  icono: string;
  /** Color de la pastilla del icono en la columna del menu. */
  color: string;
}

export interface PresentacionPantalla {
  ruta: string;
  icono: string;
}

/** Por nombre del modulo (tabla modulo). */
export const MODULOS: Record<string, PresentacionModulo> = {
  Clientes: { icono: 'people', color: '#3F51B5' },
  Cotizaciones: { icono: 'request_quote', color: '#8E44AD' },
  Eventos: { icono: 'event', color: '#00ACC1' },
  Inventarios: { icono: 'inventory_2', color: '#F39C12' },
  'Menús y platos': { icono: 'restaurant_menu', color: '#27AE60' },
  Pagos: { icono: 'payments', color: '#16A085' },
  Rentabilidad: { icono: 'trending_up', color: '#D35400' },
  Administración: { icono: 'settings', color: '#E74C3C' },
};

/** Para un modulo que se agregue en la base y todavia no tenga su presentacion aca. */
export const MODULO_SIN_PRESENTACION: PresentacionModulo = { icono: 'folder', color: '#607D8B' };

/** Por pagina_url de la opcion (tabla opcion). */
export const PANTALLAS: Record<string, PresentacionPantalla> = {
  '/api/clientes': { ruta: '/clientes', icono: 'people' },
  '/api/clientes/reportes/clientes': { ruta: '/clientes/reporte', icono: 'summarize' },
  '/api/cotizaciones': { ruta: '/cotizaciones', icono: 'request_quote' },
  '/api/cotizaciones/reportes/cotizaciones': { ruta: '/cotizaciones/reporte', icono: 'summarize' },
  '/api/eventos': { ruta: '/eventos', icono: 'event' },
  '/api/eventos/resumen': { ruta: '/eventos/estadisticas', icono: 'insights' },
  '/api/eventos/reportes/eventos': { ruta: '/eventos/reporte', icono: 'event_note' },
  '/api/inventarios': { ruta: '/inventario', icono: 'inventory_2' },
  '/api/productos': { ruta: '/inventario/productos', icono: 'category' },
  '/api/movimientos-inventario': { ruta: '/inventario/movimientos', icono: 'swap_vert' },
  '/api/movimientos-inventario/reportes/movimientos': { ruta: '/inventario/movimientos/reporte', icono: 'summarize' },
  '/api/menus': { ruta: '/menus', icono: 'restaurant_menu' },
  '/api/platos': { ruta: '/menus/platos', icono: 'restaurant' },
  '/api/bebidas': { ruta: '/menus/bebidas', icono: 'local_drink' },
  '/api/pagos': { ruta: '/pagos', icono: 'payments' },
  '/api/pagos/reportes/cuentas-por-cobrar': { ruta: '/pagos/cuentas-por-cobrar', icono: 'account_balance_wallet' },
  '/api/pagos/reportes/recibos': { ruta: '/pagos/recibos', icono: 'receipt_long' },
  '/api/rentabilidad': { ruta: '/rentabilidad', icono: 'trending_up' },
  '/api/rentabilidad/reportes/pago-personal': { ruta: '/rentabilidad/pago-personal', icono: 'badge' },
  '/api/usuarios': { ruta: '/admin/usuarios', icono: 'manage_accounts' },
  '/api/roles': { ruta: '/admin/roles', icono: 'admin_panel_settings' },
  '/api/empleados': { ruta: '/empleados', icono: 'badge' },
  '/api/vehiculos': { ruta: '/vehiculos', icono: 'local_shipping' },
  '/api/bitacora': { ruta: '/admin/bitacora', icono: 'history' },
};

/**
 * Vistas (tabla menu_vista) que se muestran como UNA pantalla que agrupa a todas sus
 * opciones: Catalogos reune los 15 catalogos y aparece si el rol tiene permiso sobre alguno.
 */
export const VISTAS_AGRUPADAS: Record<string, PresentacionPantalla> = {
  Catálogos: { ruta: '/admin/catalogos', icono: 'list_alt' },
};
