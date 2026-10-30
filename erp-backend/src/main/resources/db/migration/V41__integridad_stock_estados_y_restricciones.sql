-- Impide que el stock de un producto quede en negativo.
ALTER TABLE inventario
    ADD CONSTRAINT chk_inventario_no_negativo CHECK (cantidad_total >= 0);

-- Comprueba que el estado asignado pertenezca al tipo que usa cada tabla.
CREATE OR REPLACE FUNCTION fn_validar_tipo_estado()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM estado e
        JOIN tipo_estado t ON t.id_tipo_estado = e.id_tipo_estado
        WHERE e.id_estado = NEW.id_estado
          AND t.nombre_tipo = TG_ARGV[0]
    ) THEN
        RAISE EXCEPTION 'El estado % no corresponde a % (se esperaba un estado de tipo %)',
            NEW.id_estado, TG_TABLE_NAME, TG_ARGV[0]
            USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Asocia cada tabla con su tipo de estado.
DO $$
DECLARE
    t RECORD;
BEGIN
    FOR t IN
        SELECT * FROM (VALUES
            ('bebida', 'GENERAL'),
            ('cliente', 'GENERAL'),
            ('empleado', 'GENERAL'),
            ('evento_empleado', 'GENERAL'),
            ('menu', 'GENERAL'),
            ('metodo_pago', 'GENERAL'),
            ('plato', 'GENERAL'),
            ('usuario', 'GENERAL'),
            ('cotizacion_version', 'COTIZACION'),
            ('evento', 'EVENTO'),
            ('pago', 'PAGO'),
            ('vehiculo', 'VEHICULO')
        ) AS v(tabla, tipo)
    LOOP
        EXECUTE format(
            'CREATE TRIGGER trg_%s_tipo_estado BEFORE INSERT OR UPDATE OF id_estado ON %I
             FOR EACH ROW EXECUTE FUNCTION fn_validar_tipo_estado(%L);',
            t.tabla, t.tabla, t.tipo);
    END LOOP;
END $$;

-- Elimina la restricción única repetida sobre el nombre del módulo.
ALTER TABLE modulo DROP CONSTRAINT uq_modulo_nombre;

-- Valida para todos los clientes la regla de tener correo o teléfono.
ALTER TABLE cliente VALIDATE CONSTRAINT chk_cliente_contacto;
