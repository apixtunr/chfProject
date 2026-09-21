-- Vista con el saldo de pago por evento (total pactado, abonado y pendiente), usada por el
-- modulo de Pagos para mostrar en un solo listado que eventos todavia requieren cobro o
-- revision, sin tener que calcular el saldo evento por evento desde el frontend.
--
-- "abonado" excluye los pagos que son reembolso de un costo extra (id_costo_evento no nulo)
-- y los pagos ANULADOS, igual que el calculo que ya se hacia en el frontend (ver pago-form).
CREATE VIEW v_pago_evento AS
SELECT e.id_evento,
       e.fecha_evento,
       e.id_estado,
       est.nombre AS estado_nombre,
       te.nombre_tipo AS tipo_evento_nombre,
       cl.id_cliente,
       cl.nombre AS cliente_nombre,
       COALESCE(cv.monto_total, e.monto_menu) AS total,
       COALESCE(ab.abonado, 0) AS abonado,
       COALESCE(cv.monto_total, e.monto_menu) - COALESCE(ab.abonado, 0) AS pendiente
FROM evento e
JOIN estado est ON est.id_estado = e.id_estado
JOIN tipo_evento te ON te.id_tipo_evento = e.id_tipo_evento
LEFT JOIN cotizacion_version cv ON cv.id_cotizacion_version = e.id_cotizacion_version
LEFT JOIN cotizacion co ON co.id_cotizacion = cv.id_cotizacion
LEFT JOIN cliente cl ON cl.id_cliente = COALESCE(co.id_cliente, e.id_cliente)
LEFT JOIN (
    SELECT p.id_evento, SUM(p.monto) AS abonado
    FROM pago p
    JOIN estado es2 ON es2.id_estado = p.id_estado
    WHERE p.id_costo_evento IS NULL AND es2.nombre <> 'ANULADO'
    GROUP BY p.id_evento
) ab ON ab.id_evento = e.id_evento;
