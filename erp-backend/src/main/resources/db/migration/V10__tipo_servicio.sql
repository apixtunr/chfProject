-- ============================================================================
-- V10: separa tipo_servicio de tipo_costo. tipo_costo es para gastos internos
-- del negocio (ingredientes, renta, transporte...), usado en costo_evento y las
-- vistas de rentabilidad. tipo_servicio es para los servicios extra no-menu que
-- se le cobran al cliente en la cotizacion (bebidas, decoracion, personal...),
-- que son ingreso, no gasto. servicio_cotizacion pasa a referenciar tipo_servicio.
-- ============================================================================

CREATE TABLE tipo_servicio (
    id_tipo_servicio    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_tipo         VARCHAR(80) NOT NULL UNIQUE,
    descripcion         VARCHAR(255),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

INSERT INTO tipo_servicio (nombre_tipo, descripcion)
SELECT nombre_tipo, descripcion FROM tipo_costo
WHERE nombre_tipo IN ('Bebidas y cocteles', 'Decoracion de salon', 'Personal de servicio',
                       'Mobiliario y equipo', 'Transporte');

ALTER TABLE servicio_cotizacion ADD COLUMN id_tipo_servicio INT REFERENCES tipo_servicio(id_tipo_servicio);

-- Remapea las filas ya existentes por nombre (no por id, porque las secuencias no coinciden).
UPDATE servicio_cotizacion sc
SET id_tipo_servicio = ts.id_tipo_servicio
FROM tipo_costo tc
JOIN tipo_servicio ts ON ts.nombre_tipo = tc.nombre_tipo
WHERE sc.id_tipo_costo = tc.id_tipo_costo;

ALTER TABLE servicio_cotizacion ALTER COLUMN id_tipo_servicio SET NOT NULL;
ALTER TABLE servicio_cotizacion DROP COLUMN id_tipo_costo;

DELETE FROM tipo_costo
WHERE nombre_tipo IN ('Bebidas y cocteles', 'Decoracion de salon', 'Personal de servicio',
                       'Mobiliario y equipo', 'Transporte');

-- Registra el catalogo en la matriz de permisos de Administracion, igual que tipo_costo.
INSERT INTO opcion (id_menu_vista, nombre_opcion, orden_menu_vista, pagina_url, accion)
SELECT mv.id_menu_vista, 'Tipos de servicio', 15, '/api/tipos-servicio', 'CRUD'
FROM modulo m
JOIN menu_vista mv ON mv.id_modulo = m.id_modulo AND mv.nombre = 'Catalogos'
WHERE m.nombre = 'Administracion'
ON CONFLICT (pagina_url) DO NOTHING;
