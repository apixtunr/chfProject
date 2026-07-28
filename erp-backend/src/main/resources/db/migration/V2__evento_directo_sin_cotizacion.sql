-- ============================================================================
-- V2: Un evento puede nacer directo, sin pasar por cotizacion.
--  - id_cotizacion_version pasa a ser opcional.
--  - Se agrega id_cliente (solo se usa cuando no hay cotizacion; si la hay,
--    el cliente se deriva de cotizacion_version -> cotizacion).
--  - CHECK: todo evento debe tener cotizacion o cliente (nunca ninguno).
--
-- Escrita de forma IDEMPOTENTE porque en la base de desarrollo estos cambios
-- ya se aplicaron a mano antes de adoptar Flyway.
-- ============================================================================

ALTER TABLE evento ALTER COLUMN id_cotizacion_version DROP NOT NULL;

ALTER TABLE evento ADD COLUMN IF NOT EXISTS id_cliente INT REFERENCES cliente(id_cliente);

CREATE INDEX IF NOT EXISTS idx_evento_cliente ON evento(id_cliente);

ALTER TABLE evento DROP CONSTRAINT IF EXISTS chk_evento_origen;
ALTER TABLE evento ADD CONSTRAINT chk_evento_origen
    CHECK (id_cotizacion_version IS NOT NULL OR id_cliente IS NOT NULL);
