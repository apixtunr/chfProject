-- ============================================================================
-- V36: El menu lateral sale de la base de datos.
--
-- Hasta ahora el frontend tenia el menu escrito en su codigo (nav-items.ts) y las
-- tablas modulo, menu_vista y opcion solo se usaban para los permisos. Desde esta
-- version el menu se arma con ellas:
--
--   modulo      : cada modulo de la columna, con su icono y su color.
--   opcion      : cada pantalla del panel, con su ruta en el frontend y su icono.
--                 Una opcion sin ruta es un permiso que no es pantalla (los
--                 catalogos, ubicaciones, metodos de pago, opciones del sistema).
--   menu_vista  : una vista con ruta se muestra como UNA pantalla que agrupa a sus
--                 opciones. Es el caso de Catalogos: una sola pantalla para los 15
--                 catalogos, visible si el rol tiene permiso sobre alguno.
--
-- Ocho pantallas compartian el permiso de su modulo (los reportes, Estadisticas de
-- eventos, Cuentas por cobrar y Recibos emitidos). Ahora cada una es una opcion con
-- su propio permiso, y su pagina_url es la ruta de la API que consulta. Para que
-- nadie pierda acceso, cada rol que tenia el permiso del modulo recibe el de la
-- pantalla nueva con las mismas banderas de imprimir y exportar (que eran las que
-- gobernaban esos reportes, V33 y V34). Alta, baja y modificacion quedan en falso:
-- esas pantallas solo consultan.
--
-- Los nombres de las opciones que son pantallas pasan a ser los del menu, para que
-- Roles y permisos y el menu digan lo mismo.
-- ============================================================================

ALTER TABLE modulo ADD COLUMN icono VARCHAR(50);
ALTER TABLE modulo ADD COLUMN color VARCHAR(7);

ALTER TABLE menu_vista ADD COLUMN ruta VARCHAR(120);
ALTER TABLE menu_vista ADD COLUMN icono VARCHAR(50);

ALTER TABLE opcion ADD COLUMN ruta VARCHAR(120);
ALTER TABLE opcion ADD COLUMN icono VARCHAR(50);

-- --- Modulos: icono, color y tildes ---------------------------------------------

UPDATE modulo SET nombre = 'Menús y platos', fecha_modificacion = NOW() WHERE nombre = 'Menus y platos';
UPDATE menu_vista SET nombre = 'Menús y platos', fecha_modificacion = NOW() WHERE nombre = 'Menus y platos';

UPDATE modulo m
SET icono = v.icono, color = v.color, fecha_modificacion = NOW()
FROM (VALUES
    ('Clientes',       'people',          '#3F51B5'),
    ('Cotizaciones',   'request_quote',   '#8E44AD'),
    ('Eventos',        'event',           '#00ACC1'),
    ('Inventarios',    'inventory_2',     '#F39C12'),
    ('Menús y platos', 'restaurant_menu', '#27AE60'),
    ('Pagos',          'payments',        '#16A085'),
    ('Rentabilidad',   'trending_up',     '#D35400'),
    ('Administración', 'settings',        '#E74C3C')
) AS v (nombre, icono, color)
WHERE m.nombre = v.nombre;

-- --- Pantallas que ya eran opciones: nombre del menu, ruta, icono y orden --------

UPDATE opcion o
SET nombre_opcion = v.nombre, ruta = v.ruta, icono = v.icono, orden_menu_vista = v.orden,
    fecha_modificacion = NOW()
FROM (VALUES
    ('/api/clientes',               'Registro de Clientes',    '/clientes',               'people',               1),
    ('/api/cotizaciones',           'Gestión de Cotizaciones', '/cotizaciones',           'request_quote',        1),
    ('/api/eventos',                'Gestión Eventos',         '/eventos',                'event',                1),
    ('/api/inventarios',            'Stock',                   '/inventario',             'inventory_2',          1),
    ('/api/productos',              'Productos',               '/inventario/productos',   'category',             2),
    ('/api/movimientos-inventario', 'Movimientos',             '/inventario/movimientos', 'swap_vert',            3),
    ('/api/menus',                  'Menús',                   '/menus',                  'restaurant_menu',      1),
    ('/api/platos',                 'Platos',                  '/menus/platos',           'restaurant',           2),
    ('/api/bebidas',                'Bebidas',                 '/menus/bebidas',          'local_drink',          3),
    ('/api/pagos',                  'Pagos',                   '/pagos',                  'payments',             1),
    ('/api/rentabilidad',           'Reporte',                 '/rentabilidad',           'trending_up',          1),
    ('/api/usuarios',               'Usuarios',                '/admin/usuarios',         'manage_accounts',      1),
    ('/api/roles',                  'Roles y permisos',        '/admin/roles',            'admin_panel_settings', 2),
    ('/api/empleados',              'Empleados',               '/empleados',              'badge',                4),
    ('/api/vehiculos',              'Vehículos',               '/vehiculos',              'local_shipping',       5),
    ('/api/bitacora',               'Bitácora',                '/admin/bitacora',         'history',              6)
) AS v (pagina_url, nombre, ruta, icono, orden)
WHERE o.pagina_url = v.pagina_url;

-- Permisos que no son pantalla: se corren al final de su vista.
UPDATE opcion SET orden_menu_vista = 3, fecha_modificacion = NOW() WHERE pagina_url = '/api/ubicaciones';
UPDATE opcion SET orden_menu_vista = 4, fecha_modificacion = NOW() WHERE pagina_url = '/api/metodos-pago';

-- Catalogos: una sola pantalla para todas las opciones de su vista.
UPDATE menu_vista
SET nombre = 'Catálogos', ruta = '/admin/catalogos', icono = 'list_alt', fecha_modificacion = NOW()
WHERE nombre IN ('Catalogos', 'Catálogos');

-- --- Pantallas nuevas, cada una con su permiso ----------------------------------

CREATE TEMPORARY TABLE pantalla_nueva (
    pagina_url  VARCHAR(255),
    nombre      VARCHAR(80),
    ruta        VARCHAR(120),
    icono       VARCHAR(50),
    orden       INT,
    del_modulo  VARCHAR(255)   -- pagina_url de la opcion cuyo permiso se copia
) ON COMMIT DROP;

INSERT INTO pantalla_nueva VALUES
    ('/api/clientes/reportes/clientes',               'Reporte de clientes',     '/clientes/reporte',               'summarize',              2, '/api/clientes'),
    ('/api/cotizaciones/reportes/cotizaciones',       'Reporte de cotizaciones', '/cotizaciones/reporte',           'summarize',              2, '/api/cotizaciones'),
    ('/api/eventos/resumen',                          'Estadísticas de eventos', '/eventos/estadisticas',           'insights',               2, '/api/eventos'),
    ('/api/eventos/reportes/eventos',                 'Reporte de eventos',      '/eventos/reporte',                'event_note',             3, '/api/eventos'),
    ('/api/movimientos-inventario/reportes/movimientos', 'Reporte de movimientos', '/inventario/movimientos/reporte', 'summarize',            4, '/api/movimientos-inventario'),
    ('/api/pagos/reportes/cuentas-por-cobrar',        'Cuentas por cobrar',      '/pagos/cuentas-por-cobrar',       'account_balance_wallet', 2, '/api/pagos'),
    ('/api/pagos/reportes/recibos',                   'Recibos emitidos',        '/pagos/recibos',                  'receipt_long',           3, '/api/pagos'),
    ('/api/rentabilidad/reportes/pago-personal',      'Pago al personal',        '/rentabilidad/pago-personal',     'badge',                  2, '/api/rentabilidad');

INSERT INTO opcion (id_menu_vista, nombre_opcion, orden_menu_vista, pagina_url, accion, ruta, icono)
SELECT base.id_menu_vista, p.nombre, p.orden, p.pagina_url, 'CONSULTA', p.ruta, p.icono
FROM pantalla_nueva p
JOIN opcion base ON base.pagina_url = p.del_modulo
WHERE NOT EXISTS (SELECT 1 FROM opcion o WHERE o.pagina_url = p.pagina_url);

INSERT INTO rol_opcion (id_rol, id_opcion, alta, baja, modificacion, imprimir, exportar)
SELECT ro.id_rol, nueva.id_opcion, FALSE, FALSE, FALSE, ro.imprimir, ro.exportar
FROM pantalla_nueva p
JOIN opcion base ON base.pagina_url = p.del_modulo
JOIN rol_opcion ro ON ro.id_opcion = base.id_opcion
JOIN opcion nueva ON nueva.pagina_url = p.pagina_url
ON CONFLICT (id_rol, id_opcion) DO NOTHING;
