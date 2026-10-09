-- ============================================================================
-- V33: Permisos de impresion y exportacion para los reportes por modulo.
--
-- Cada modulo tiene ahora su reporte en tabla (por periodo, con filtros y
-- agrupado por dia, semana, mes o año), que se descarga en PDF (permiso
-- imprimir) o en CSV para Excel (permiso exportar):
--
--   Clientes        : Clientes registrados                  -> Ventas
--   Cotizaciones    : Cotizaciones por periodo              -> Ventas (ya imprimia)
--   Eventos         : Eventos por periodo y Hoja de servicio -> Ventas, Cocina, Almacén, Operativo
--   Inventario      : Movimientos de inventario             -> Almacén
--   Pagos           : Recibos emitidos y Cuentas por cobrar -> Ventas y Finanzas
--   Rentabilidad    : Pago al personal                      -> Finanzas (ya tenia ambos, V18)
--
-- Exportar se da a quien analiza los datos de su area (Ventas en clientes,
-- cotizaciones y eventos; Finanzas en pagos; Almacén en movimientos). Cocina y
-- Operativo solo imprimen la hoja de servicio y el listado de eventos.
--
-- Solo se encienden permisos sobre filas que ya existen: no se le da acceso a un
-- modulo a ningun rol que no lo tuviera, ni se apaga nada que el administrador haya
-- configurado. ADMINISTRADOR no depende de rol_opcion.
-- ============================================================================

UPDATE rol_opcion ro
SET imprimir = TRUE
FROM rol r, opcion o
WHERE ro.id_rol = r.id_rol
  AND ro.id_opcion = o.id_opcion
  AND (
        (o.pagina_url = '/api/clientes' AND r.nombre_rol IN ('VENTAS'))
     OR (o.pagina_url = '/api/cotizaciones' AND r.nombre_rol IN ('VENTAS'))
     OR (o.pagina_url = '/api/eventos' AND r.nombre_rol IN ('VENTAS', 'COCINA', 'ALMACÉN', 'OPERATIVO'))
     OR (o.pagina_url = '/api/movimientos-inventario' AND r.nombre_rol IN ('ALMACÉN'))
     OR (o.pagina_url = '/api/pagos' AND r.nombre_rol IN ('VENTAS', 'FINANZAS'))
  );

UPDATE rol_opcion ro
SET exportar = TRUE
FROM rol r, opcion o
WHERE ro.id_rol = r.id_rol
  AND ro.id_opcion = o.id_opcion
  AND (
        (o.pagina_url IN ('/api/clientes', '/api/cotizaciones', '/api/eventos') AND r.nombre_rol = 'VENTAS')
     OR (o.pagina_url = '/api/pagos' AND r.nombre_rol = 'FINANZAS')
     OR (o.pagina_url = '/api/movimientos-inventario' AND r.nombre_rol = 'ALMACÉN')
  );
