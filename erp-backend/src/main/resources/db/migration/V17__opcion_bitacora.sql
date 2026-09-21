-- ============================================================================
-- V17: Siembra la opcion de pantalla para la nueva Bitacora (accesos y
-- movimientos), dentro del modulo Administracion. Sigue el mismo patron
-- idempotente de V3 (ON CONFLICT por pagina_url).
-- ============================================================================

INSERT INTO opcion (id_menu_vista, nombre_opcion, orden_menu_vista, pagina_url, accion)
SELECT mv.id_menu_vista, 'Bitacora', 6, '/api/bitacora', 'CRUD'
FROM modulo m
JOIN menu_vista mv ON mv.id_modulo = m.id_modulo AND mv.nombre = m.nombre
WHERE m.nombre = 'Administracion'
ON CONFLICT (pagina_url) DO NOTHING;
