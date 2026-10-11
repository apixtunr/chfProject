-- ============================================================================
-- V35: Tildes en los nombres del modulo de Administracion.
--
-- El modulo, su vista y la vista de catalogos se sembraron sin tilde (V3, V4),
-- igual que la opcion de vehiculos. Se ven en Roles y permisos, asi que se
-- corrigen: Administración, Catálogos y Vehículos.
-- ============================================================================

UPDATE modulo
SET nombre = 'Administración', fecha_modificacion = NOW()
WHERE nombre = 'Administracion';

UPDATE menu_vista
SET nombre = 'Administración', fecha_modificacion = NOW()
WHERE nombre = 'Administracion';

UPDATE menu_vista
SET nombre = 'Catálogos', fecha_modificacion = NOW()
WHERE nombre = 'Catalogos';

UPDATE opcion
SET nombre_opcion = 'Vehículos', fecha_modificacion = NOW()
WHERE nombre_opcion = 'Vehiculos';
