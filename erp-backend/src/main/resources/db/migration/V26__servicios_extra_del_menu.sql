-- ============================================================================
-- V26: Los servicios extra que se cotizan son los del menu de la empresa.
--
-- El catalogo de tipos de servicio venia de una lista generica (bar, decoracion,
-- mobiliario, transporte, meseros) que no es lo que vende la empresa. Su menu
-- 2026 dice que el transporte va incluido, que los vasos son solo para bebidas
-- naturales, que los cocineros no atienden mesas, y que lo que se cobra aparte
-- es: hora extra de cocinero, cubremantel extra, personal de cocina adicional
-- (por ejemplo para una segunda entrada de buffet) y la prueba de menu.
--
--  1. tipo_servicio.activo: un tipo que ya no se ofrece pero tiene historial
--     no se puede borrar; se desactiva. Las cotizaciones viejas lo siguen
--     mostrando y ya no aparece al cotizar.
--  2. "Personal de servicio" pasa a "Personal de cocina adicional", que es lo
--     que describen las lineas que ya lo usan ("2 personas extras en el
--     evento"), y absorbe "Segunda entrada de buffet" (V24): segun el menu, la
--     segunda entrada se cobra como personal de cocina adicional.
--  3. Se agrega "Prueba de menu (4 personas)".
--  4. Lo que no esta en el menu se borra si nunca se uso, o se desactiva.
-- ============================================================================

-- 1. Activo ------------------------------------------------------------------
ALTER TABLE tipo_servicio ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
COMMENT ON COLUMN tipo_servicio.activo IS 'false = ya no se ofrece al cotizar; se conserva por las cotizaciones que lo usan';

-- 2. Personal de cocina adicional -------------------------------------------
UPDATE tipo_servicio
SET nombre_tipo = 'Personal de cocina adicional',
    descripcion = 'Cocineros adicionales para el evento, por ejemplo para atender una segunda entrada de buffet',
    fecha_modificacion = NOW()
WHERE lower(nombre_tipo) = 'personal de servicio'
  AND NOT EXISTS (SELECT 1 FROM tipo_servicio WHERE lower(nombre_tipo) = 'personal de cocina adicional');

INSERT INTO tipo_servicio (nombre_tipo, descripcion)
SELECT 'Personal de cocina adicional',
       'Cocineros adicionales para el evento, por ejemplo para atender una segunda entrada de buffet'
WHERE NOT EXISTS (SELECT 1 FROM tipo_servicio WHERE lower(nombre_tipo) = 'personal de cocina adicional');

UPDATE servicio_cotizacion
SET id_tipo_servicio = (SELECT id_tipo_servicio FROM tipo_servicio WHERE nombre_tipo = 'Personal de cocina adicional')
WHERE id_tipo_servicio IN (SELECT id_tipo_servicio FROM tipo_servicio
                           WHERE lower(nombre_tipo) = 'segunda entrada de buffet');
DELETE FROM tipo_servicio WHERE lower(nombre_tipo) = 'segunda entrada de buffet';

-- 3. Prueba de menu ----------------------------------------------------------
INSERT INTO tipo_servicio (nombre_tipo, descripcion)
SELECT 'Prueba de menú (4 personas)',
       'Se reserva con un mes de anticipación; su costo es el precio del menú elegido'
WHERE NOT EXISTS (SELECT 1 FROM tipo_servicio WHERE lower(nombre_tipo) LIKE 'prueba de men%');

-- 4. Lo que no es del menu ---------------------------------------------------
CREATE TEMP TABLE servicios_del_menu (nombre VARCHAR(80)) ON COMMIT DROP;
INSERT INTO servicios_del_menu VALUES
    ('hora extra de cocinero'),
    ('cubremantel extra para buffet'),
    ('personal de cocina adicional'),
    ('prueba de menú (4 personas)');

DELETE FROM tipo_servicio t
WHERE lower(t.nombre_tipo) NOT IN (SELECT nombre FROM servicios_del_menu)
  AND NOT EXISTS (SELECT 1 FROM servicio_cotizacion s WHERE s.id_tipo_servicio = t.id_tipo_servicio);

UPDATE tipo_servicio t
SET activo = FALSE, fecha_modificacion = NOW()
WHERE lower(t.nombre_tipo) NOT IN (SELECT nombre FROM servicios_del_menu);

-- De paso: el comentario de V22 decia "precio por porcion"; el menu cobra por persona.
COMMENT ON COLUMN plato.unidad_venta IS 'PERSONA (precio por persona), CIENTO (precio por 100 unidades) o UNIDAD';
