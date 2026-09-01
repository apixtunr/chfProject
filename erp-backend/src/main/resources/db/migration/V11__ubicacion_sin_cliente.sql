-- ============================================================================
-- V11: Ubicacion nunca se uso realmente como "direccion guardada de un
-- cliente" (id_cliente siempre quedo en NULL): las ubicaciones son salones o
-- venues externos que cada cotizacion/evento captura por su cuenta, no
-- propiedad del cliente. Se quita el vinculo.
-- ============================================================================

ALTER TABLE ubicacion DROP COLUMN id_cliente;
