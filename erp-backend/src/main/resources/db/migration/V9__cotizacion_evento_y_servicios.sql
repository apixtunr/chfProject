-- ============================================================================
-- V9: La cotizacion pasa a capturar tipo de evento, ubicacion y cantidad de
-- personas desde el momento de cotizar (antes solo se sabia hasta crear el
-- Evento). Tambien se agregan servicios extra no-menu (bebidas, decoracion,
-- personal, etc.) cotizables por version, igual que detalle_cotizacion pero
-- sin platos, y su monto se suma al monto_total de la version.
-- ============================================================================

ALTER TABLE cotizacion ADD COLUMN id_tipo_evento INT REFERENCES tipo_evento(id_tipo_evento);
ALTER TABLE cotizacion ADD COLUMN id_ubicacion INT REFERENCES ubicacion(id_ubicacion);
ALTER TABLE cotizacion ADD COLUMN cantidad_personas INT CHECK (cantidad_personas > 0);

-- Backfill desde el evento ya generado, cuando existe
UPDATE cotizacion c
SET id_tipo_evento = e.id_tipo_evento,
    id_ubicacion = e.id_ubicacion,
    cantidad_personas = e.cantidad_personas
FROM evento e
JOIN cotizacion_version cv ON cv.id_cotizacion_version = e.id_cotizacion_version
WHERE cv.id_cotizacion = c.id_cotizacion;

-- Cotizaciones de prueba sin evento aun: se completan con un valor por defecto
UPDATE cotizacion SET id_tipo_evento = 1, id_ubicacion = 1, cantidad_personas = 30
WHERE id_tipo_evento IS NULL;

ALTER TABLE cotizacion ALTER COLUMN id_tipo_evento SET NOT NULL;
ALTER TABLE cotizacion ALTER COLUMN id_ubicacion SET NOT NULL;
ALTER TABLE cotizacion ALTER COLUMN cantidad_personas SET NOT NULL;

-- Catalogo de conceptos para servicios extra de la cotizacion (bebidas, decoracion, personal...)
INSERT INTO tipo_costo (nombre_tipo, descripcion) VALUES
    ('Bebidas y cocteles', 'Servicio de bar y bebidas para el evento'),
    ('Decoracion de salon', 'Decoracion y ambientacion del lugar'),
    ('Personal de servicio', 'Meseros y personal de atencion durante el evento'),
    ('Mobiliario y equipo', 'Renta de mesas, sillas, manteleria, etc.'),
    ('Transporte', 'Traslado de personal, equipo e insumos');

CREATE TABLE servicio_cotizacion (
    id_servicio_cotizacion INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cotizacion_version  INT NOT NULL REFERENCES cotizacion_version(id_cotizacion_version),
    id_tipo_costo          INT NOT NULL REFERENCES tipo_costo(id_tipo_costo),
    descripcion            VARCHAR(255),
    monto                  NUMERIC(12,2) NOT NULL CHECK (monto >= 0),
    fecha_creacion         TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion     TIMESTAMP
);

CREATE TRIGGER trg_servicio_cotizacion_fecha_mod BEFORE UPDATE ON servicio_cotizacion
FOR EACH ROW EXECUTE FUNCTION fn_set_fecha_modificacion();

-- El monto_total de la version ahora suma tambien los servicios extra
CREATE OR REPLACE FUNCTION fn_actualizar_monto_version()
RETURNS TRIGGER AS $$
DECLARE v_id INT;
BEGIN
    v_id := COALESCE(NEW.id_cotizacion_version, OLD.id_cotizacion_version);
    UPDATE cotizacion_version
       SET monto_total = COALESCE((
            SELECT SUM(subtotal) FROM detalle_cotizacion
            WHERE id_cotizacion_version = v_id), 0)
          + COALESCE((
            SELECT SUM(monto) FROM servicio_cotizacion
            WHERE id_cotizacion_version = v_id), 0)
     WHERE id_cotizacion_version = v_id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_servicio_cotizacion_monto_total
AFTER INSERT OR UPDATE OR DELETE ON servicio_cotizacion
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_monto_version();
