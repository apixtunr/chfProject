-- ============================================================================
-- V3: Siembra la estructura de seguridad (modulo -> menu_vista -> opcion) con
-- todas las pantallas construidas en el frontend, para poder configurar
-- permisos de roles desde la matriz de administracion.
-- pagina_url debe coincidir EXACTAMENTE con lo que usan PermisoService
-- (backend) y AuthService/nav-items (frontend).
-- Idempotente: se apoya en restricciones UNIQUE + ON CONFLICT DO NOTHING.
-- ============================================================================

-- Unicidad necesaria para que el seed sea idempotente (y para integridad general)
ALTER TABLE modulo     DROP CONSTRAINT IF EXISTS uq_modulo_nombre;
ALTER TABLE modulo     ADD CONSTRAINT uq_modulo_nombre UNIQUE (nombre);
ALTER TABLE menu_vista DROP CONSTRAINT IF EXISTS uq_menu_vista_modulo_nombre;
ALTER TABLE menu_vista ADD CONSTRAINT uq_menu_vista_modulo_nombre UNIQUE (id_modulo, nombre);
ALTER TABLE opcion     DROP CONSTRAINT IF EXISTS uq_opcion_pagina_url;
ALTER TABLE opcion     ADD CONSTRAINT uq_opcion_pagina_url UNIQUE (pagina_url);

-- 1. Modulos (los 8 del alcance)
INSERT INTO modulo (nombre, orden) VALUES
 ('Clientes', 1),
 ('Cotizaciones', 2),
 ('Eventos', 3),
 ('Inventarios', 4),
 ('Menus y platos', 5),
 ('Pagos', 6),
 ('Rentabilidad', 7),
 ('Administracion', 8)
ON CONFLICT (nombre) DO NOTHING;

-- 2. Un menu_vista principal por modulo
INSERT INTO menu_vista (id_modulo, nombre, orden)
SELECT m.id_modulo, m.nombre, m.orden FROM modulo m
ON CONFLICT (id_modulo, nombre) DO NOTHING;

-- 3. Opciones (pantallas). accion documenta el CRUD disponible.
INSERT INTO opcion (id_menu_vista, nombre_opcion, orden_menu_vista, pagina_url, accion)
SELECT mv.id_menu_vista, o.nombre_opcion, o.orden, o.pagina_url, 'CRUD'
FROM (VALUES
    -- (modulo,            nombre_opcion,               orden, pagina_url)
    ('Clientes',        'Clientes',                  1, '/api/clientes'),
    ('Clientes',        'Ubicaciones',               2, '/api/ubicaciones'),
    ('Cotizaciones',    'Cotizaciones',              1, '/api/cotizaciones'),
    ('Eventos',         'Eventos',                   1, '/api/eventos'),
    ('Inventarios',     'Stock de inventario',       1, '/api/inventarios'),
    ('Inventarios',     'Productos',                 2, '/api/productos'),
    ('Inventarios',     'Movimientos de inventario', 3, '/api/movimientos-inventario'),
    ('Menus y platos',  'Menus',                     1, '/api/menus'),
    ('Menus y platos',  'Platos',                    2, '/api/platos'),
    ('Pagos',           'Pagos',                     1, '/api/pagos'),
    ('Pagos',           'Metodos de pago',           2, '/api/metodos-pago'),
    ('Rentabilidad',    'Reporte de rentabilidad',   1, '/api/rentabilidad'),
    ('Administracion',  'Usuarios',                  1, '/api/usuarios'),
    ('Administracion',  'Roles y permisos',          2, '/api/roles'),
    ('Administracion',  'Opciones del sistema',      3, '/api/opciones'),
    ('Administracion',  'Empleados',                 4, '/api/empleados'),
    ('Administracion',  'Vehiculos',                 5, '/api/vehiculos')
) AS o(modulo, nombre_opcion, orden, pagina_url)
JOIN modulo m ON m.nombre = o.modulo
JOIN menu_vista mv ON mv.id_modulo = m.id_modulo AND mv.nombre = m.nombre
ON CONFLICT (pagina_url) DO NOTHING;
