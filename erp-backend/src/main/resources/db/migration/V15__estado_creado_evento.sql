-- ============================================================================
-- V15: nuevo estado CREADO para evento, previo a PLANIFICADO. Un evento nace
-- CREADO; solo pasa a PLANIFICADO cuando el usuario confirma manualmente que
-- ya tiene Menu, Personal, Vehiculos e Inventario asignados (ver
-- EventoServiceImpl.planificar). Si llega la fecha/hora de inicio y sigue
-- CREADO (nunca se confirmo), se cancela solo -no puede arrancar un evento
-- que no se termino de armar.
-- ============================================================================

INSERT INTO estado (id_tipo_estado, nombre)
SELECT id_tipo_estado, 'CREADO' FROM tipo_estado WHERE nombre_tipo = 'EVENTO';
