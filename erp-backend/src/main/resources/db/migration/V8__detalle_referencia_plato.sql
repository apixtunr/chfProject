-- ============================================================================
-- V8: El detalle de cotizacion/evento pasa a referenciar un plato especifico
-- dentro del menu (antes solo se elegia el menu y el precio se escribia a
-- mano). El precio ahora sale siempre de menu_plato.
-- ============================================================================

-- Datos de prueba existentes: se limpian porque no tienen id_plato asignado
-- (el sistema todavia no esta en produccion).
DELETE FROM detalle_cotizacion;
DELETE FROM detalle_evento;

ALTER TABLE detalle_cotizacion ADD COLUMN id_plato INT REFERENCES plato(id_plato);
ALTER TABLE detalle_cotizacion ALTER COLUMN id_plato SET NOT NULL;
ALTER TABLE detalle_cotizacion ADD CONSTRAINT fk_detalle_cot_menu_plato
    FOREIGN KEY (id_menu, id_plato) REFERENCES menu_plato(id_menu, id_plato);

ALTER TABLE detalle_evento ADD COLUMN id_plato INT REFERENCES plato(id_plato);
ALTER TABLE detalle_evento ALTER COLUMN id_plato SET NOT NULL;
ALTER TABLE detalle_evento ADD CONSTRAINT fk_detalle_evt_menu_plato
    FOREIGN KEY (id_menu, id_plato) REFERENCES menu_plato(id_menu, id_plato);
