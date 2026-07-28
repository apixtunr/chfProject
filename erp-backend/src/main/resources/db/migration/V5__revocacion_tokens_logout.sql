-- ============================================================================
-- V5: Revocacion de tokens al cerrar sesion.
-- JWT es stateless: sin esto, un token sigue siendo valido hasta su expiracion
-- aunque el usuario haya cerrado sesion. Al hacer logout se marca
-- tokens_validos_desde = NOW() y el filtro JWT rechaza cualquier token emitido
-- antes de esa marca. NULL = ningun logout registrado, todos los tokens valen.
-- ============================================================================

ALTER TABLE usuario ADD COLUMN IF NOT EXISTS tokens_validos_desde TIMESTAMP;
