-- ============================================================================
-- V12: v_costos_evento no incluia el costo de los insumos realmente consumidos
-- (evento_inventario + producto.precio_unitario), asi que la ganancia por evento
-- en Rentabilidad quedaba inflada. Solo cuenta lo ya CONFIRMADO (fecha_consumo no
-- nulo): lo que sigue "Planificado" todavia no se gasto de verdad.
-- ============================================================================

CREATE OR REPLACE VIEW v_costos_evento AS
SELECT e.id_evento,
       COALESCE(SUM(c.monto), 0)
         + COALESCE((SELECT SUM(ee.salario_evento) FROM evento_empleado ee
                     WHERE ee.id_evento = e.id_evento), 0)
         + COALESCE((SELECT SUM(ei.cantidad * p.precio_unitario)
                     FROM evento_inventario ei
                     JOIN producto p ON p.id_producto = ei.id_producto
                     WHERE ei.id_evento = e.id_evento
                       AND ei.fecha_consumo IS NOT NULL), 0) AS total_costos
FROM evento e
LEFT JOIN costo_evento c ON c.id_evento = e.id_evento
GROUP BY e.id_evento;
