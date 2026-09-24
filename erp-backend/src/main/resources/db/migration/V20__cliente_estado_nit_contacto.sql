-- ============================================================================
-- V20: Reglas de negocio de Clientes.
--
--  1. Estado ACTIVO/INACTIVO. Un cliente ya no se borra: se inactiva. Uno
--     inactivo no se puede usar en cotizaciones ni eventos nuevos, pero todo su
--     historial sigue visible. Se usa el tipo de estado GENERAL, igual que los
--     catalogos.
--  2. NIT normalizado y unico. Se guarda siempre como "cuerpo-verificador"
--     (ej. 6769359-8) o "CF" (consumidor final), que pasa a ser el valor por
--     defecto cuando no se indica NIT. Todos los NIT distintos de CF son unicos.
--  3. Al menos un medio de contacto (telefono o correo).
--
-- Idempotente en lo posible y sin perder clientes: si hay NIT repetidos se
-- conserva en el cliente mas antiguo y los demas pasan a CF (queda aviso en el
-- log de Flyway con sus ids para corregirlos a mano).
-- ============================================================================

-- 1. Estado -------------------------------------------------------------------
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS id_estado INT REFERENCES estado(id_estado);

UPDATE cliente
SET id_estado = (SELECT e.id_estado FROM estado e
                 JOIN tipo_estado t ON t.id_tipo_estado = e.id_tipo_estado
                 WHERE t.nombre_tipo = 'GENERAL' AND e.nombre = 'ACTIVO')
WHERE id_estado IS NULL;

ALTER TABLE cliente ALTER COLUMN id_estado SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_cliente_estado ON cliente(id_estado);

-- 2. NIT ----------------------------------------------------------------------
-- Vacios a NULL, y luego NULL a CF.
UPDATE cliente SET nit = NULL WHERE nit IS NOT NULL AND btrim(nit) = '';
UPDATE cliente SET nit = 'CF' WHERE nit IS NULL OR upper(regexp_replace(nit, '[\s.-]', '', 'g')) IN ('CF', 'C/F');

-- Formato canonico: sin espacios ni puntos, en mayusculas y con guion antes del
-- digito verificador.
UPDATE cliente
SET nit = substr(limpio, 1, length(limpio) - 1) || '-' || right(limpio, 1)
FROM (SELECT id_cliente AS id, upper(regexp_replace(nit, '[\s.-]', '', 'g')) AS limpio FROM cliente) n
WHERE cliente.id_cliente = n.id
  AND cliente.nit <> 'CF'
  AND n.limpio ~ '^[0-9]+[0-9K]$';

-- NIT repetidos: se queda con el cliente mas antiguo.
DO $$
DECLARE
    repetidos TEXT;
BEGIN
    SELECT string_agg(id_cliente::text, ', ') INTO repetidos
    FROM (SELECT id_cliente,
                 row_number() OVER (PARTITION BY nit ORDER BY id_cliente) AS orden
          FROM cliente WHERE nit <> 'CF') x
    WHERE orden > 1;

    IF repetidos IS NOT NULL THEN
        RAISE NOTICE 'V20: clientes con NIT repetido pasados a CF (revisar): %', repetidos;
        UPDATE cliente SET nit = 'CF'
        WHERE id_cliente IN (
            SELECT id_cliente FROM (
                SELECT id_cliente, row_number() OVER (PARTITION BY nit ORDER BY id_cliente) AS orden
                FROM cliente WHERE nit <> 'CF') y
            WHERE orden > 1);
    END IF;
END $$;

ALTER TABLE cliente ALTER COLUMN nit SET DEFAULT 'CF';
ALTER TABLE cliente ALTER COLUMN nit SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_cliente_nit ON cliente(nit) WHERE nit <> 'CF';

-- 3. Contacto -----------------------------------------------------------------
UPDATE cliente SET correo = NULL WHERE correo IS NOT NULL AND btrim(correo) = '';
UPDATE cliente SET telefono = NULL WHERE telefono IS NOT NULL AND btrim(telefono) = '';

-- NOT VALID: se exige a partir de ahora (altas y ediciones) sin rechazar la
-- migracion por clientes viejos que no tengan ninguno; al editarlos se pedira.
ALTER TABLE cliente DROP CONSTRAINT IF EXISTS chk_cliente_contacto;
ALTER TABLE cliente ADD CONSTRAINT chk_cliente_contacto
    CHECK (correo IS NOT NULL OR telefono IS NOT NULL) NOT VALID;
