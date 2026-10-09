-- ============================================================================
-- V33: Permisos de impresion y exportacion para los reportes por modulo.
--
-- Los reportes nuevos se descargan en PDF (permiso imprimir) y en CSV (permiso
-- exportar) dentro del modulo al que pertenecen:
--
--   Eventos      : Hoja de servicio del evento. La usan quienes preparan el evento
--                  (Ventas, Cocina, Almacén y Operativo); no lleva montos.
--   Pagos        : Cuentas por cobrar y Cobros por periodo (Ventas imprime; Finanzas
--                  imprime y exporta).
--   Rentabilidad : Pago al personal. Finanzas ya tenia imprimir y exportar (V18).
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
        (o.pagina_url = '/api/eventos' AND r.nombre_rol IN ('VENTAS', 'COCINA', 'ALMACÉN', 'OPERATIVO'))
     OR (o.pagina_url = '/api/pagos' AND r.nombre_rol IN ('VENTAS', 'FINANZAS'))
  );

UPDATE rol_opcion ro
SET exportar = TRUE
FROM rol r, opcion o
WHERE ro.id_rol = r.id_rol
  AND ro.id_opcion = o.id_opcion
  AND o.pagina_url = '/api/pagos'
  AND r.nombre_rol = 'FINANZAS';
