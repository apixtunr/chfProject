-- ============================================================================
-- V27: Cotizaciones que vencen y versiones que se reemplazan.
--
--  1. VENCIDA: la cotizacion vale 15 dias desde que se envia (vigencia del
--     menu de la empresa, app.cotizacion.vigencia-dias). Al enviarla se guarda
--     cuando se envio y hasta que dia vale; una tarea automatica pasa a VENCIDA
--     las ENVIADAS cuya vigencia ya paso. Guardar la fecha limite (y no
--     calcularla) respeta lo que se le prometio al cliente aunque despues se
--     cambie la vigencia.
--  2. REEMPLAZADA: si el cliente pide cambios a una cotizacion enviada, se crea
--     una version nueva y la enviada pasa sola a REEMPLAZADA, en vez de tener
--     que marcarla RECHAZADA cuando el cliente no la rechazo.
-- ============================================================================

INSERT INTO estado (id_tipo_estado, nombre)
SELECT t.id_tipo_estado, n.nombre
FROM tipo_estado t
CROSS JOIN (VALUES ('VENCIDA'), ('REEMPLAZADA')) AS n(nombre)
WHERE t.nombre_tipo = 'COTIZACION'
  AND NOT EXISTS (SELECT 1 FROM estado e WHERE e.id_tipo_estado = t.id_tipo_estado AND e.nombre = n.nombre);

ALTER TABLE cotizacion_version ADD COLUMN fecha_envio TIMESTAMP;
ALTER TABLE cotizacion_version ADD COLUMN vigente_hasta DATE;
COMMENT ON COLUMN cotizacion_version.fecha_envio IS 'Cuando se envio al cliente';
COMMENT ON COLUMN cotizacion_version.vigente_hasta IS 'Ultimo dia en que el cliente puede aceptarla; despues pasa a VENCIDA';

-- Las que estan ENVIADAS no guardaron cuando se enviaron. La ultima modificacion
-- de la version es el cambio a ENVIADA (el detalle se congela al enviar), asi que
-- es la mejor aproximacion. Las ya respondidas se quedan sin fecha de envio: su
-- ultima modificacion es la respuesta del cliente, no el envio.
UPDATE cotizacion_version cv
SET fecha_envio = COALESCE(cv.fecha_modificacion, cv.fecha_version),
    vigente_hasta = CAST(COALESCE(cv.fecha_modificacion, cv.fecha_version) AS DATE) + 15
FROM estado e
WHERE e.id_estado = cv.id_estado AND e.nombre = 'ENVIADA';
