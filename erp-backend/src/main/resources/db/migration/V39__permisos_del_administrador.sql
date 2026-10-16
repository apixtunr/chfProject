-- ============================================================================
-- V39: El ADMINISTRADOR tiene sus permisos en rol_opcion, como los demas roles.
--
-- Hasta ahora el backend y el frontend le daban acceso total por el nombre del
-- rol, sin mirar rol_opcion. La pantalla de permisos le mostraba las casillas
-- vacias y lo que se marcara ahi no tenia ningun efecto. Desde esta version el
-- ADMINISTRADOR tiene exactamente lo que dicen sus casillas, igual que VENTAS o
-- COCINA, y por la naturaleza del rol arranca con todas marcadas.
--
-- Se marcan todas las pantallas que existen hoy. Si alguna fila ya existia (por
-- ejemplo, porque alguien marco casillas desde la pantalla), queda completa.
--
-- Ojo para las versiones siguientes: una pantalla nueva en opcion tambien hay que
-- darsela al ADMINISTRADOR en la misma migracion; si no, no la vera.
-- ============================================================================

INSERT INTO rol_opcion (id_rol, id_opcion, alta, baja, modificacion, imprimir, exportar)
SELECT r.id_rol, o.id_opcion, TRUE, TRUE, TRUE, TRUE, TRUE
FROM rol r
CROSS JOIN opcion o
WHERE r.nombre_rol = 'ADMINISTRADOR'
ON CONFLICT (id_rol, id_opcion) DO UPDATE
SET alta = TRUE,
    baja = TRUE,
    modificacion = TRUE,
    imprimir = TRUE,
    exportar = TRUE,
    fecha_modificacion = NOW();
