-- ============================================================================
-- V6: Detalle de menu para eventos directos (sin cotizacion previa)
-- ============================================================================

ALTER TABLE evento ADD COLUMN monto_menu NUMERIC(12,2) NOT NULL DEFAULT 0;

CREATE TABLE detalle_evento (
    id_detalle_evento    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_evento            INT NOT NULL REFERENCES evento(id_evento),
    id_menu              INT NOT NULL REFERENCES menu(id_menu),
    cantidad_platos      INT NOT NULL CHECK (cantidad_platos > 0),
    precio_unitario      NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal             NUMERIC(12,2) GENERATED ALWAYS AS (cantidad_platos * precio_unitario) STORED,
    observaciones        VARCHAR(255),
    fecha_creacion       TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion   TIMESTAMP
);

CREATE INDEX idx_detalle_evento_evento ON detalle_evento(id_evento);

CREATE TRIGGER trg_detalle_evento_fecha_mod BEFORE UPDATE ON detalle_evento
FOR EACH ROW EXECUTE FUNCTION fn_set_fecha_modificacion();

CREATE OR REPLACE FUNCTION fn_actualizar_monto_evento()
RETURNS TRIGGER AS $$
DECLARE v_id INT;
BEGIN
    v_id := COALESCE(NEW.id_evento, OLD.id_evento);
    UPDATE evento
       SET monto_menu = COALESCE((
            SELECT SUM(subtotal) FROM detalle_evento
            WHERE id_evento = v_id), 0)
     WHERE id_evento = v_id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_detalle_evento_monto_total
AFTER INSERT OR UPDATE OR DELETE ON detalle_evento
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_monto_evento();
