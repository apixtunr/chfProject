-- ============================================================================
-- V13: liga un pago con el costo_evento que reembolsa (cuando el cliente cubre
-- un costo extra). NULL significa que es un abono normal al precio del evento,
-- no un reembolso. Permite separar, en un pago, cuanto es abono al menu vs.
-- cuanto es devolucion de un gasto especifico.
-- ============================================================================

ALTER TABLE pago ADD COLUMN id_costo_evento INT REFERENCES costo_evento(id_costo_evento);

CREATE INDEX idx_pago_costo_evento ON pago(id_costo_evento);
