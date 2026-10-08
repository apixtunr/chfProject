-- ============================================================================
-- V30: El rol BODEGA pasa a llamarse ALMACÉN.
--
-- "Bodega" nombraba el lugar y no el area de trabajo; ALMACÉN es el termino que se
-- usa en administracion y combina con los demas roles (VENTAS, COCINA, FINANZAS).
-- Solo cambia el nombre: el id del rol, sus permisos en rol_opcion y los usuarios
-- que lo tienen asignado quedan igual. El sistema no depende del nombre de este rol.
-- ============================================================================

UPDATE rol SET nombre_rol = 'ALMACÉN' WHERE nombre_rol = 'BODEGA';
