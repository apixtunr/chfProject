-- ============================================================================
-- V4: Registra los catalogos simples como opciones del sistema, bajo un
-- menu_vista "Catalogos" del modulo Administracion, para poder asignarles
-- permisos por rol desde la matriz. Idempotente (mismas UNIQUE de V3).
-- ============================================================================

INSERT INTO menu_vista (id_modulo, nombre, orden)
SELECT m.id_modulo, 'Catalogos', 10 FROM modulo m WHERE m.nombre = 'Administracion'
ON CONFLICT (id_modulo, nombre) DO NOTHING;

INSERT INTO opcion (id_menu_vista, nombre_opcion, orden_menu_vista, pagina_url, accion)
SELECT mv.id_menu_vista, o.nombre_opcion, o.orden, o.pagina_url, 'CRUD'
FROM (VALUES
    ('Departamentos',            1,  '/api/departamentos'),
    ('Municipios',               2,  '/api/municipios'),
    ('Tipos de estado',          3,  '/api/tipos-estado'),
    ('Estados',                  4,  '/api/estados'),
    ('Generos',                  5,  '/api/generos'),
    ('Tipos de documento',       6,  '/api/tipos-documento'),
    ('Tipos de evento',          7,  '/api/tipos-evento'),
    ('Tipos de costo',           8,  '/api/tipos-costo'),
    ('Puestos de empleado',      9,  '/api/puestos-empleado'),
    ('Marcas de vehiculo',       10, '/api/marcas-vehiculo'),
    ('Lineas de vehiculo',       11, '/api/lineas-vehiculo'),
    ('Tipos de placa',           12, '/api/tipos-placa'),
    ('Tipos de inventario',      13, '/api/tipos-inventario'),
    ('Categorias de producto',   14, '/api/categorias-producto')
) AS o(nombre_opcion, orden, pagina_url)
JOIN modulo m ON m.nombre = 'Administracion'
JOIN menu_vista mv ON mv.id_modulo = m.id_modulo AND mv.nombre = 'Catalogos'
ON CONFLICT (pagina_url) DO NOTHING;

-- /api/metodos-pago ya fue registrado en V3 bajo el modulo Pagos.
