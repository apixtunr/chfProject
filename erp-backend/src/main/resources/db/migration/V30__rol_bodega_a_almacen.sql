-- ===========================================================================
-- Renombra el rol BODEGA a ALMACEN.
--
-- El cambio ya estaba aplicado en la base de la nube pero su archivo nunca se
-- agrego al repositorio, de modo que una instalacion desde cero quedaba con el
-- nombre antiguo. Se reconstruye aqui para que las migraciones vuelvan a
-- describir el esquema completo.
--
-- Solo cambia la etiqueta visible del rol: los permisos de rol_opcion apuntan a
-- id_rol, no al nombre, asi que no hay que tocarlos. El codigo tampoco compara
-- contra el literal 'BODEGA' (solo lo menciona en comentarios).
-- ===========================================================================

UPDATE rol
   SET nombre_rol       = 'ALMACÉN',
       fecha_modificacion = CURRENT_TIMESTAMP
 WHERE nombre_rol = 'BODEGA';
