-- ============================================================================
-- V14: v_ingresos_evento sumaba TODOS los pagos sin importar el estado. Un pago
-- ANULADO (cheque rebotado, transferencia reversada) no es dinero real que entro
-- a la empresa, asi que no deberia contar como ingreso. Ya no existe PENDIENTE
-- como estado inicial (registrar un pago ya lo confirma), asi que el unico caso
-- a excluir es ANULADO.
-- ============================================================================

CREATE OR REPLACE VIEW v_ingresos_evento AS
SELECT e.id_evento,
       COALESCE(SUM(p.monto) FILTER (WHERE es.nombre <> 'ANULADO'), 0) AS total_ingresos
FROM evento e
LEFT JOIN pago p ON p.id_evento = e.id_evento
LEFT JOIN estado es ON es.id_estado = p.id_estado
GROUP BY e.id_evento;
