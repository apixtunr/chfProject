-- ============================================================================
-- V37: Se quitan las columnas de presentacion que V36 agrego al menu.
--
-- V36 agrego icono y color a modulo, y ruta e icono a menu_vista y a opcion.
-- Son datos de diseño (como se dibuja el menu), no del negocio, y cambiaban la
-- estructura de tablas que ya estaban definidas. Se eliminan: las tres tablas
-- vuelven a su estructura original.
--
-- El menu sigue saliendo de la base: que modulos y pantallas hay, sus nombres,
-- su orden y que rol ve cada una (rol_opcion). El icono, el color y la ruta de
-- cada pantalla los pone el frontend a partir de la pagina_url de la opcion.
--
-- Se conservan los datos de V36: las opciones de los reportes, Estadisticas de
-- eventos, Cuentas por cobrar y Recibos emitidos, con sus permisos por rol, y
-- los nombres de las opciones iguales a los del menu.
-- ============================================================================

ALTER TABLE modulo DROP COLUMN icono;
ALTER TABLE modulo DROP COLUMN color;

ALTER TABLE menu_vista DROP COLUMN ruta;
ALTER TABLE menu_vista DROP COLUMN icono;

ALTER TABLE opcion DROP COLUMN ruta;
ALTER TABLE opcion DROP COLUMN icono;
