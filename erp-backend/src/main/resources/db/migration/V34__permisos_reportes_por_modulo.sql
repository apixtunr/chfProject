-- ============================================================================
-- V34: Permisos para los reportes en tabla de cada modulo.
--
-- V33 encendio imprimir/exportar para la hoja de servicio y los reportes de
-- Pagos. Ahora cada modulo tiene su reporte en tabla (por periodo, con filtros y
-- agrupado por dia, semana, mes o año), que se descarga en PDF (imprimir) o en
-- CSV para Excel (exportar):
--
--   Clientes      : Clientes registrados                  -> Ventas
--   Cotizaciones  : Cotizaciones por periodo              -> Ventas (ya imprimia, V18)
--   Eventos       : Eventos por periodo                   -> Ventas, Cocina, Almacén, Operativo (V33)
--   Inventario    : Movimientos de inventario             -> Almacén
--   Pagos         : Recibos emitidos y Cuentas por cobrar -> Ventas y Finanzas (V33)
--   Rentabilidad  : Pago al personal                      -> Finanzas (V18)
--
-- Exportar se da a quien analiza los datos de su area: Ventas en clientes,
-- cotizaciones y eventos; Almacén en movimientos (Finanzas ya exporta pagos y
-- rentabilidad).
--
-- Igual que V33: solo se encienden permisos sobre filas que ya existen.
-- ============================================================================

UPDATE rol_opcion ro
SET imprimir = TRUE
FROM rol r, opcion o
WHERE ro.id_rol = r.id_rol
  AND ro.id_opcion = o.id_opcion
  AND (
        (o.pagina_url = '/api/clientes' AND r.nombre_rol = 'VENTAS')
     OR (o.pagina_url = '/api/movimientos-inventario' AND r.nombre_rol = 'ALMACÉN')
  );

UPDATE rol_opcion ro
SET exportar = TRUE
FROM rol r, opcion o
WHERE ro.id_rol = r.id_rol
  AND ro.id_opcion = o.id_opcion
  AND (
        (o.pagina_url IN ('/api/clientes', '/api/cotizaciones', '/api/eventos') AND r.nombre_rol = 'VENTAS')
     OR (o.pagina_url = '/api/movimientos-inventario' AND r.nombre_rol = 'ALMACÉN')
  );
