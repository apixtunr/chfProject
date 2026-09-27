-- ============================================================================
-- V28: Las bebidas pasan a ser un catalogo, igual que los platos.
--
-- Hasta V25 la bebida era texto: plato.bebidas guardaba "Te frio|Rosa de
-- Jamaica" y cada linea de cotizacion o de evento guardaba el nombre elegido.
-- La lista de bebidas posibles estaba escrita en el frontend, asi que agregar
-- una bebida nueva o corregir un nombre exigia tocar codigo, y un nombre mal
-- escrito en una linea no apuntaba a nada.
--
--  1. bebida: catalogo con estado (ACTIVO/INACTIVO), como plato.
--  2. plato_bebida: que bebidas incluye cada plato, como menu_plato. Sin filas
--     = el plato no incluye bebida (boquitas).
--  3. detalle_cotizacion.id_bebida y detalle_evento.id_bebida reemplazan al
--     texto: la bebida que eligio el cliente para ese plato.
--  4. Se quita plato.bebidas.
--
-- Las bebidas de cada plato salen del menu de la empresa (Menu-Banquetes La
-- Casa Del Chef 2026):
--   - te frio o rosa de Jamaica: aves, cerditos, especialidades, parrilladas,
--     chapin, pastas y refacciones;
--   - solo te frio: milanesa de pollo gratinada con queso;
--   - solo rosa de Jamaica: quesadillas de jamon y queso;
--   - menu de ninos: "sera la misma del menu de adulto elegido", que en todos
--     los menus de adulto es te frio o rosa de Jamaica;
--   - atol de platano: refacciones chapinas;
--   - jugo de naranja y estacion de cafe: desayunos (vienen las dos juntas, no
--     se elige una, por eso es una sola bebida del catalogo);
--   - boquitas: ninguna.
-- ============================================================================

-- 1. Catalogo --------------------------------------------------------------
CREATE TABLE bebida (
    id_bebida           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_estado           INT NOT NULL REFERENCES estado(id_estado),
    nombre_bebida       VARCHAR(80) NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);
CREATE UNIQUE INDEX uq_bebida_nombre ON bebida (lower(nombre_bebida));

INSERT INTO bebida (id_estado, nombre_bebida)
SELECT e.id_estado, b.nombre
FROM (VALUES ('Té frío'), ('Rosa de Jamaica'), ('Atol de plátano'), ('Jugo de naranja y estación de café'))
     AS b(nombre)
JOIN estado e ON e.nombre = 'ACTIVO'
JOIN tipo_estado t ON t.id_tipo_estado = e.id_tipo_estado AND t.nombre_tipo = 'GENERAL';

-- Nombre anterior (V25) -> nombre del catalogo. Tambien se agrega cualquier otra
-- bebida que se hubiera escrito a mano, para no perder nada.
CREATE TEMP TABLE nombre_bebida_anterior (anterior VARCHAR(120), actual VARCHAR(80)) ON COMMIT DROP;
INSERT INTO nombre_bebida_anterior VALUES ('jugo de naranja y café', 'Jugo de naranja y estación de café');

INSERT INTO bebida (id_estado, nombre_bebida)
SELECT DISTINCT e.id_estado, trim(x.nombre)
FROM (
    SELECT unnest(string_to_array(bebidas, '|')) AS nombre FROM plato
    UNION SELECT bebida FROM detalle_cotizacion
    UNION SELECT bebida FROM detalle_evento
) x
JOIN estado e ON e.nombre = 'ACTIVO'
JOIN tipo_estado t ON t.id_tipo_estado = e.id_tipo_estado AND t.nombre_tipo = 'GENERAL'
WHERE trim(coalesce(x.nombre, '')) <> ''
  AND lower(trim(x.nombre)) NOT IN (SELECT anterior FROM nombre_bebida_anterior)
  AND NOT EXISTS (SELECT 1 FROM bebida b WHERE lower(b.nombre_bebida) = lower(trim(x.nombre)));

-- id de una bebida escrita como texto (con el nombre anterior ya traducido).
CREATE FUNCTION pg_temp.id_bebida(texto VARCHAR) RETURNS INT AS $$
    SELECT b.id_bebida FROM bebida b
    WHERE lower(b.nombre_bebida) = lower(coalesce(
        (SELECT n.actual FROM nombre_bebida_anterior n WHERE n.anterior = lower(trim(texto))),
        trim(texto)))
$$ LANGUAGE SQL STABLE;

-- 2. Bebidas de cada plato ---------------------------------------------------
CREATE TABLE plato_bebida (
    id_plato            INT NOT NULL REFERENCES plato(id_plato),
    id_bebida           INT NOT NULL REFERENCES bebida(id_bebida),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    PRIMARY KEY (id_plato, id_bebida)
);

-- Lo que ya tenia cada plato (incluye platos creados a mano fuera del menu).
INSERT INTO plato_bebida (id_plato, id_bebida)
SELECT DISTINCT p.id_plato, pg_temp.id_bebida(x.nombre)
FROM plato p
CROSS JOIN LATERAL unnest(string_to_array(p.bebidas, '|')) AS x(nombre)
WHERE trim(x.nombre) <> ''
ON CONFLICT DO NOTHING;

-- Y para los platos del menu de la empresa, lo que dice el menu.
CREATE TEMP TABLE bebidas_del_menu (menu VARCHAR(120), plato VARCHAR(120), bebida VARCHAR(80)) ON COMMIT DROP;
INSERT INTO bebidas_del_menu (menu, plato, bebida)
SELECT m.menu, NULL, b.bebida
FROM (VALUES ('menu aves'), ('menu cerditos'), ('menu especialidades'), ('menu parrilladas'),
             ('menu chapín'), ('menu pastas'), ('menu niños'), ('menu refacciones')) AS m(menu)
CROSS JOIN (VALUES ('Té frío'), ('Rosa de Jamaica')) AS b(bebida)
UNION ALL SELECT 'menu refacciones chapinas', NULL, 'Atol de plátano'
UNION ALL SELECT 'menu desayunos', NULL, 'Jugo de naranja y estación de café'
-- Excepciones: el plato manda sobre su menu.
UNION ALL SELECT NULL, 'milanesa de pollo gratinada con queso', 'Té frío'
UNION ALL SELECT NULL, 'quesadillas de jamón y queso', 'Rosa de Jamaica';

CREATE TEMP TABLE platos_del_menu ON COMMIT DROP AS
SELECT DISTINCT p.id_plato,
       coalesce(
           (SELECT array_agg(bm.bebida) FROM bebidas_del_menu bm WHERE bm.plato = lower(p.nombre_plato)),
           (SELECT array_agg(DISTINCT bm.bebida) FROM bebidas_del_menu bm
            JOIN menu_plato mp ON mp.id_plato = p.id_plato
            JOIN menu m ON m.id_menu = mp.id_menu
            WHERE bm.menu = lower(m.nombre_menu))) AS bebidas
FROM plato p
WHERE EXISTS (SELECT 1 FROM bebidas_del_menu bm WHERE bm.plato = lower(p.nombre_plato))
   OR EXISTS (SELECT 1 FROM menu_plato mp JOIN menu m ON m.id_menu = mp.id_menu
              WHERE mp.id_plato = p.id_plato
                AND lower(m.nombre_menu) IN (SELECT menu FROM bebidas_del_menu WHERE menu IS NOT NULL));

DELETE FROM plato_bebida pb USING platos_del_menu pm WHERE pb.id_plato = pm.id_plato;
INSERT INTO plato_bebida (id_plato, id_bebida)
SELECT pm.id_plato, pg_temp.id_bebida(x.nombre)
FROM platos_del_menu pm
CROSS JOIN LATERAL unnest(pm.bebidas) AS x(nombre)
ON CONFLICT DO NOTHING;

-- Las boquitas no incluyen bebida.
DELETE FROM plato_bebida pb
USING menu_plato mp, menu m
WHERE mp.id_plato = pb.id_plato AND m.id_menu = mp.id_menu AND lower(m.nombre_menu) = 'menu boquitas';

-- 3. Bebida elegida en cada linea ----------------------------------------------
ALTER TABLE detalle_cotizacion ADD COLUMN id_bebida INT REFERENCES bebida(id_bebida);
ALTER TABLE detalle_evento ADD COLUMN id_bebida INT REFERENCES bebida(id_bebida);

UPDATE detalle_cotizacion SET id_bebida = pg_temp.id_bebida(bebida) WHERE trim(coalesce(bebida, '')) <> '';
UPDATE detalle_evento SET id_bebida = pg_temp.id_bebida(bebida) WHERE trim(coalesce(bebida, '')) <> '';

ALTER TABLE detalle_cotizacion DROP COLUMN bebida;
ALTER TABLE detalle_evento DROP COLUMN bebida;

-- 4. Fuera el texto del plato ---------------------------------------------------
ALTER TABLE plato DROP COLUMN bebidas;

-- 5. Pantalla de bebidas y sus permisos (igual que Platos) ----------------------
INSERT INTO opcion (id_menu_vista, nombre_opcion, orden_menu_vista, pagina_url, accion)
SELECT mv.id_menu_vista, 'Bebidas', 3, '/api/bebidas', 'CRUD'
FROM modulo m
JOIN menu_vista mv ON mv.id_modulo = m.id_modulo AND mv.nombre = m.nombre
WHERE m.nombre = 'Menus y platos'
ON CONFLICT (pagina_url) DO NOTHING;

-- Quien tiene Platos recibe lo mismo sobre Bebidas.
INSERT INTO rol_opcion (id_rol, id_opcion, alta, baja, modificacion, imprimir, exportar)
SELECT ro.id_rol, nueva.id_opcion, ro.alta, ro.baja, ro.modificacion, ro.imprimir, ro.exportar
FROM rol_opcion ro
JOIN opcion platos ON platos.id_opcion = ro.id_opcion AND platos.pagina_url = '/api/platos'
CROSS JOIN opcion nueva
WHERE nueva.pagina_url = '/api/bebidas'
ON CONFLICT (id_rol, id_opcion) DO NOTHING;

COMMENT ON TABLE bebida IS 'Bebidas que pueden incluir los platos';
COMMENT ON TABLE plato_bebida IS 'Bebidas que incluye cada plato; sin filas = no incluye bebida';
COMMENT ON COLUMN detalle_cotizacion.id_bebida IS 'Bebida elegida para este plato (una de plato_bebida)';
COMMENT ON COLUMN detalle_evento.id_bebida IS 'Bebida elegida para este plato (una de plato_bebida)';
