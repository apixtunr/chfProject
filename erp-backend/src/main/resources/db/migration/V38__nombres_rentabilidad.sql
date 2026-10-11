-- ============================================================================
-- V38: Nombres de las pantallas de Rentabilidad.
--
-- Solo datos: el menu y Roles y permisos muestran el nombre de la opcion.
--   Reporte          -> Análisis de rentabilidad
--   Pago al personal -> Reporte de Pagos al Personal
-- ============================================================================

UPDATE opcion
SET nombre_opcion = 'Análisis de rentabilidad', fecha_modificacion = NOW()
WHERE pagina_url = '/api/rentabilidad';

UPDATE opcion
SET nombre_opcion = 'Reporte de Pagos al Personal', fecha_modificacion = NOW()
WHERE pagina_url = '/api/rentabilidad/reportes/pago-personal';
