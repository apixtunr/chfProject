-- ============================================================================
-- V6: Renombra el estado de cotizacion BORRADOR a CREADA
-- ============================================================================

UPDATE estado
SET nombre = 'CREADA'
WHERE nombre = 'BORRADOR'
  AND id_tipo_estado = (SELECT id_tipo_estado FROM tipo_estado WHERE nombre_tipo = 'COTIZACION');
