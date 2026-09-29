-- ============================================================================
-- V29: Anticipo al planificar y cancelacion con acuerdo.
--
--  1. planificado_sin_anticipo: la empresa cobra el 50% una semana antes del
--     evento. Planificar sin ese 50% queda a criterio del usuario, pero se
--     marca aqui; la bitacora guarda quien lo decidio.
--  2. Cancelacion: no hay una politica fija de devolucion, se negocia con el
--     cliente. Se registra el motivo y en que quedo el anticipo (retenido,
--     devuelto o devuelto en parte, y cuanto).
-- ============================================================================

ALTER TABLE evento ADD COLUMN planificado_sin_anticipo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE evento ADD COLUMN motivo_cancelacion VARCHAR(255);
ALTER TABLE evento ADD COLUMN acuerdo_anticipo VARCHAR(20);
ALTER TABLE evento ADD COLUMN monto_devuelto NUMERIC(12,2);

ALTER TABLE evento ADD CONSTRAINT chk_evento_acuerdo_anticipo
    CHECK (acuerdo_anticipo IN ('SIN_ANTICIPO', 'RETENIDO', 'DEVUELTO', 'DEVUELTO_PARCIAL'));
ALTER TABLE evento ADD CONSTRAINT chk_evento_monto_devuelto CHECK (monto_devuelto >= 0);

COMMENT ON COLUMN evento.planificado_sin_anticipo IS 'Se planifico sin tener pagado el 50%, por decision del usuario';
COMMENT ON COLUMN evento.motivo_cancelacion IS 'Por que se cancelo el evento';
COMMENT ON COLUMN evento.acuerdo_anticipo IS 'En que quedo lo pagado al cancelar: SIN_ANTICIPO, RETENIDO, DEVUELTO o DEVUELTO_PARCIAL';
COMMENT ON COLUMN evento.monto_devuelto IS 'Lo que se devolvio al cliente al cancelar';
