-- ============================================================================
-- V25: La bebida va con cada plato, no con la cotizacion.
--
-- V24 puso una sola bebida para toda la cotizacion, pero en el menu de la
-- empresa la bebida viene incluida en cada plato ("Lomo relleno... Bebida: te
-- frio o rosa Jamaica") y no todos ofrecen lo mismo: la milanesa solo te frio,
-- las refacciones chapinas atol, los desayunos jugo y cafe, las boquitas
-- ninguna. En un mismo evento hay invitados con platos distintos, cada uno con
-- su bebida.
--
--  1. plato.bebidas: las bebidas que ofrece el plato, separadas por '|'
--     ("Te frio|Rosa de Jamaica"). NULL = no incluye bebida. Se cargan del
--     menu 2026 por nombre de menu y, para las excepciones, de plato.
--  2. detalle_cotizacion.bebida y detalle_evento.bebida: la que eligio el
--     cliente para ese plato.
--  3. Se pasa a las lineas la bebida que se hubiera puesto en la cotizacion
--     (si el plato la ofrece) y se quitan cotizacion.bebida y evento.bebida.
-- ============================================================================

-- 1. Bebidas que ofrece cada plato ------------------------------------------
ALTER TABLE plato ADD COLUMN bebidas VARCHAR(120);
COMMENT ON COLUMN plato.bebidas IS 'Bebidas incluidas, separadas por |; NULL = no incluye bebida';

UPDATE plato p
SET bebidas = 'Té frío|Rosa de Jamaica'
FROM menu_plato mp
JOIN menu m ON m.id_menu = mp.id_menu
WHERE mp.id_plato = p.id_plato
  AND lower(m.nombre_menu) IN ('menu aves', 'menu cerditos', 'menu especialidades', 'menu parrilladas',
                               'menu chapín', 'menu pastas', 'menu niños', 'menu refacciones');

UPDATE plato p
SET bebidas = 'Atol de plátano'
FROM menu_plato mp
JOIN menu m ON m.id_menu = mp.id_menu
WHERE mp.id_plato = p.id_plato AND lower(m.nombre_menu) = 'menu refacciones chapinas';

UPDATE plato p
SET bebidas = 'Jugo de naranja y café'
FROM menu_plato mp
JOIN menu m ON m.id_menu = mp.id_menu
WHERE mp.id_plato = p.id_plato AND lower(m.nombre_menu) = 'menu desayunos';

-- Excepciones del menu.
UPDATE plato SET bebidas = 'Té frío' WHERE lower(nombre_plato) = 'milanesa de pollo gratinada con queso';
UPDATE plato SET bebidas = 'Rosa de Jamaica' WHERE lower(nombre_plato) = 'quesadillas de jamón y queso';

-- 2. Bebida elegida en cada linea ---------------------------------------------
ALTER TABLE detalle_cotizacion ADD COLUMN bebida VARCHAR(60);
ALTER TABLE detalle_evento ADD COLUMN bebida VARCHAR(60);

-- 3. Lo que se hubiera guardado a nivel de cotizacion/evento pasa a sus platos.
UPDATE detalle_cotizacion d
SET bebida = c.bebida
FROM cotizacion_version cv
JOIN cotizacion c ON c.id_cotizacion = cv.id_cotizacion
JOIN plato p ON TRUE
WHERE d.id_cotizacion_version = cv.id_cotizacion_version
  AND p.id_plato = d.id_plato
  AND c.bebida IS NOT NULL
  AND d.bebida IS NULL
  AND ('|' || p.bebidas || '|') LIKE ('%|' || c.bebida || '|%');

UPDATE detalle_evento d
SET bebida = e.bebida
FROM evento e, plato p
WHERE d.id_evento = e.id_evento
  AND p.id_plato = d.id_plato
  AND e.bebida IS NOT NULL
  AND d.bebida IS NULL
  AND ('|' || p.bebidas || '|') LIKE ('%|' || e.bebida || '|%');

ALTER TABLE cotizacion DROP COLUMN bebida;
ALTER TABLE evento DROP COLUMN bebida;
