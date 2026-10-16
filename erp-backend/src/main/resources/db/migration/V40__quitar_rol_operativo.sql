-- ============================================================================
-- V40: Se quita el rol OPERATIVO.
--
-- Era uno de los dos roles de ejemplo del esquema inicial (V1). Cuando en V18 se
-- definieron los roles por area de trabajo (VENTAS, COCINA, ALMACÉN, FINANZAS),
-- OPERATIVO quedo sin un area de la empresa que le corresponda. Los roles del
-- sistema quedan en cinco: ADMINISTRADOR y uno por area.
--
-- Sus permisos eran de clientes y eventos, lo mismo que atiende VENTAS, asi que los
-- usuarios que lo tenian pasan a VENTAS. Despues se borran sus permisos y el rol.
-- Solo las tablas usuario y rol_opcion apuntan a rol.
-- ============================================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol = 'VENTAS') THEN
        RAISE EXCEPTION 'V40: no existe el rol VENTAS para pasar a los usuarios de OPERATIVO';
    END IF;
END $$;

UPDATE usuario u
SET id_rol = (SELECT id_rol FROM rol WHERE nombre_rol = 'VENTAS'),
    fecha_modificacion = NOW()
FROM rol r
WHERE r.id_rol = u.id_rol
  AND r.nombre_rol = 'OPERATIVO';

DELETE FROM rol_opcion ro
USING rol r
WHERE r.id_rol = ro.id_rol
  AND r.nombre_rol = 'OPERATIVO';

DELETE FROM rol WHERE nombre_rol = 'OPERATIVO';
