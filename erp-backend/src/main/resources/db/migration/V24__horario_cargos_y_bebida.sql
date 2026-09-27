-- ============================================================================
-- V24: Horario del servicio, cargos con precio fijo y bebida del menu.
--
--  1. Hora de inicio en la cotizacion. El menu de la empresa ofrece servicios
--     de 4 horas que empiezan en punto entre las 11:00 y las 19:00 y terminan
--     a mas tardar a las 21:00 (22:00 si empiezan a las 18 o 19). La hora de
--     fin no se guarda: se calcula (CondicionesComerciales). El evento que
--     nace de la cotizacion la recibe.
--
--  2. Cargos con precio fijo. Los servicios extra tenian solo un monto libre;
--     ahora un tipo de servicio puede tener precio unitario (hora extra de
--     cocinero Q25.00, cubremantel extra Q35.00) y la linea lleva cantidad:
--     monto = cantidad x precio. Los tipos sin precio siguen con monto libre.
--
--  3. Bebida. Cada menu incluye una bebida a elegir (te frio o rosa de
--     Jamaica); cocina necesita saber cual. Va en la cotizacion y en el evento.
-- ============================================================================

ALTER TABLE cotizacion ADD COLUMN hora_inicio TIME;
ALTER TABLE cotizacion ADD COLUMN bebida VARCHAR(60);
ALTER TABLE evento ADD COLUMN bebida VARCHAR(60);

ALTER TABLE tipo_servicio ADD COLUMN precio_unitario NUMERIC(12,2);
ALTER TABLE tipo_servicio ADD CONSTRAINT chk_tipo_servicio_precio CHECK (precio_unitario >= 0);

ALTER TABLE servicio_cotizacion ADD COLUMN cantidad INT NOT NULL DEFAULT 1;
ALTER TABLE servicio_cotizacion ADD CONSTRAINT chk_servicio_cotizacion_cantidad CHECK (cantidad > 0);

COMMENT ON COLUMN cotizacion.hora_inicio IS 'Inicio del servicio de 4 horas; el fin se calcula';
COMMENT ON COLUMN tipo_servicio.precio_unitario IS 'Precio por unidad (ej. por cocinero y hora); NULL = monto libre';
COMMENT ON COLUMN servicio_cotizacion.cantidad IS 'Unidades cotizadas; con precio fijo, monto = cantidad x precio';

-- Cargos del menu de la empresa. El cubremantel no tiene precio en el menu:
-- Q35.00 es una referencia de alquiler en Guatemala, editable en Catalogos.
INSERT INTO tipo_servicio (nombre_tipo, descripcion, precio_unitario)
SELECT c.nombre, c.descripcion, c.precio
FROM (VALUES
        ('Hora extra de cocinero', 'Por cocinero y por hora, despues de las 4 horas de servicio', 25.00),
        ('Cubremantel extra para buffet', 'Alquiler, para combinar el buffet con el mobiliario del evento', 35.00),
        ('Segunda entrada de buffet', 'Personal de cocina adicional para atender un segundo buffet', NULL)
     ) AS c(nombre, descripcion, precio)
WHERE NOT EXISTS (SELECT 1 FROM tipo_servicio t WHERE lower(t.nombre_tipo) = lower(c.nombre));
