-- ============================================================================
-- V22: Precios como los vende la empresa, y su catalogo del menu 2026.
--
--  1. Precio por escala. Casi todos los platos tienen dos precios en el menu
--     de la empresa: uno para eventos de menos de 100 personas y otro, menor,
--     desde 100 personas ("Desde 50 personas a Q45.00, desde 100 personas a
--     Q40.00"). menu_plato.precio_unitario pasa a ser el precio base y
--     precio_desde_100 el de volumen; NULL = el plato tiene un solo precio
--     (menu de ninos, refacciones, desayunos, boquitas). La escala la decide
--     el total de personas del evento, no la cantidad de cada plato.
--
--  2. Unidad de venta. No todo se vende por persona: las boquitas se venden
--     por ciento ("Q800.00 el ciento") y algunas por unidad ("Bola de queso
--     Q75.00"). plato.unidad_venta dice como leer su precio; la cantidad que
--     se cotiza siempre va en porciones o unidades.
--
--  3. Catalogo 2026. Se carga el menu de la empresa con sus dos precios. Se
--     busca por nombre (sin distinguir mayusculas): lo que ya existe se
--     actualiza y lo que falta se crea, asi que en una base que ya tenia el
--     menu no se duplica nada. Los precios de las cotizaciones y eventos ya
--     registrados no cambian: cada linea guarda el precio con que se cotizo.
-- ============================================================================

-- 1 y 2. Esquema -------------------------------------------------------------
ALTER TABLE plato ADD COLUMN unidad_venta VARCHAR(10) NOT NULL DEFAULT 'PERSONA';
ALTER TABLE plato ADD CONSTRAINT chk_plato_unidad_venta CHECK (unidad_venta IN ('PERSONA', 'CIENTO', 'UNIDAD'));

ALTER TABLE menu_plato ADD COLUMN precio_desde_100 NUMERIC(12,2);
ALTER TABLE menu_plato ADD CONSTRAINT chk_menu_plato_precio_desde_100 CHECK (precio_desde_100 >= 0);

COMMENT ON COLUMN plato.unidad_venta IS 'PERSONA (precio por porcion), CIENTO (precio por 100 unidades) o UNIDAD';
COMMENT ON COLUMN menu_plato.precio_unitario IS 'Precio base: eventos de menos de 100 personas (o unico precio)';
COMMENT ON COLUMN menu_plato.precio_desde_100 IS 'Precio para eventos desde 100 personas; NULL = mismo precio base';

-- 3. Catalogo ----------------------------------------------------------------
CREATE TEMP TABLE catalogo_2026 (
    menu          VARCHAR(120),
    orden         INT,
    plato         VARCHAR(120),
    unidad        VARCHAR(10),
    precio_base   NUMERIC(12,2),
    precio_100    NUMERIC(12,2)
) ON COMMIT DROP;

INSERT INTO catalogo_2026 (menu, orden, plato, unidad, precio_base, precio_100) VALUES
    ('Menu Aves', 1, 'Pollo cuadril y pierna al horno bañado con salsa de champiñones', 'PERSONA', 40, 35),
    ('Menu Aves', 2, 'Jugoso pollo cuadril y pierna Frito, bañado de mayonesa con chipotle', 'PERSONA', 40, 35),
    ('Menu Aves', 3, 'Jugoso pollo cuadril y pierna a la parrilla bañado con una liviana salsa de tomate con un toque de chile jalapeño', 'PERSONA', 40, 35),
    ('Menu Aves', 4, 'Jugosas fajitas de pollo con una exquisita salsa de queso con cebolla, chile pimiento y champiñones', 'PERSONA', 45, 40),
    ('Menu Aves', 5, 'Exquisito pollo entero a las brasas para 6 personas bañado con salsa de tomate con aroma a hierbas', 'PERSONA', 40, 38),
    ('Menu Aves', 6, 'Enrollado de pollo cuadril y pierna bañados con salsa de champiñones', 'PERSONA', 42, 38),
    ('Menu Aves', 7, 'Pechuga de pollo Cordón bleu', 'PERSONA', 55, 50),
    ('Menu Aves', 8, 'Fajitas de pechuga de pollo a la plancha', 'PERSONA', 45, 40),
    ('Menu Aves', 9, 'Milanesa de pollo gratinada con queso', 'PERSONA', 45, 40),

    ('Menu Cerditos', 1, 'Lomo de cerdo al horno', 'PERSONA', 42, 38),
    ('Menu Cerditos', 2, 'Lomo relleno', 'PERSONA', 45, 40),
    ('Menu Cerditos', 3, 'Pierna de cerdo al horno', 'PERSONA', 42, 38),
    ('Menu Cerditos', 4, 'Jugoso lomo de cerdo relleno de jamón y queso bañado con una liviana salsa de tomate', 'PERSONA', 45, 40),
    ('Menu Cerditos', 5, 'Jugoso lomo mechado relleno de verduras bañado con salsa de champiñones', 'PERSONA', 45, 40),
    ('Menu Cerditos', 6, 'Costilla a la barbacoa', 'PERSONA', 50, 45),

    ('Menu Especialidades', 1, '2 carnes: pechuga de pollo rellena de jamón y queso y lomo de cerdo al horno', 'PERSONA', 55, 50),
    ('Menu Especialidades', 2, 'Costilla de cerdo a la barbacoa y pollo al horno', 'PERSONA', 60, 55),
    ('Menu Especialidades', 3, 'Festival de pollo al horno con salsa de tomate y lomo de cerdo al horno con salsa de champiñones', 'PERSONA', 55, 50),
    ('Menu Especialidades', 4, 'Cerdito a las brasas', 'PERSONA', 105, 95),
    ('Menu Especialidades', 5, 'Jugosa pierna de cerdo a las brasas con 2 salsas de tomate y salsa barbacoa', 'PERSONA', 47, 42),
    ('Menu Especialidades', 6, 'Exquisita costillas de cerdo a las brasas con 2 salsas de tomate y barbacoa', 'PERSONA', 50, 45),

    ('Menu Parrilladas', 1, 'Mar y tierra', 'PERSONA', 100, 95),
    ('Menu Parrilladas', 2, 'Parrillada mixta de puyazo y costilla', 'PERSONA', 90, 85),
    ('Menu Parrilladas', 3, 'Puyazo importado', 'PERSONA', 90, 85),

    ('Menu Chapín', 1, 'Carne picada mixta de cerdo y carne de res', 'PERSONA', 40, 35),
    ('Menu Chapín', 2, 'Hilachas hechas en casa, como las de la abuelita', 'PERSONA', 42, 38),
    ('Menu Chapín', 3, 'Pepián de pollo hecho en casa', 'PERSONA', 42, 38),
    ('Menu Chapín', 4, 'Pepián de res', 'PERSONA', 50, 45),
    ('Menu Chapín', 5, 'Pollo en jocón', 'PERSONA', 42, 38),
    ('Menu Chapín', 6, 'Kak ik de pavo', 'PERSONA', 65, 60),

    ('Menu Pastas', 1, 'Canelones rellenos de pollo bañados con salsa de tomate o salsa de queso', 'PERSONA', 45, 40),
    ('Menu Pastas', 2, 'Lasaña de res', 'PERSONA', 45, 40),
    ('Menu Pastas', 3, 'Pasta corta con pollo y salsa de queso', 'PERSONA', 43, 38),
    ('Menu Pastas', 4, 'Pasta espagueti con camarones con salsa de tomate y queso parmesano', 'PERSONA', 80, 75),
    ('Menu Pastas', 5, 'Pasta espagueti con cubitos de pollo y champiñones con salsa blanca', 'PERSONA', 40, 35),

    ('Menu Niños', 1, 'Queso burguesa especial', 'PERSONA', 35, NULL),
    ('Menu Niños', 2, 'Hot dog especial', 'PERSONA', 30, NULL),
    ('Menu Niños', 3, 'Quesadillas de jamón y queso', 'PERSONA', 35, NULL),
    ('Menu Niños', 4, 'Lasaña de res para niño', 'PERSONA', 33, NULL),
    ('Menu Niños', 5, 'Festival de nachos para niño', 'PERSONA', 35, NULL),

    ('Menu Refacciones', 1, 'Pan con jamón y queso', 'PERSONA', 30, NULL),
    ('Menu Refacciones', 2, 'Pan con pollo', 'PERSONA', 30, NULL),
    ('Menu Refacciones', 3, 'Chuchitos de pollo', 'PERSONA', 30, NULL),

    ('Menu Refacciones Chapinas', 1, 'Festival chapín', 'PERSONA', 35, NULL),
    ('Menu Refacciones Chapinas', 2, 'Festival es mi tierra', 'PERSONA', 35, NULL),
    ('Menu Refacciones Chapinas', 3, 'Festival de tostadas y tacos', 'PERSONA', 35, NULL),

    ('Menu Desayunos', 1, 'Huevos revueltos con tomate y cebolla con salsa ranchera', 'PERSONA', 40, NULL),
    ('Menu Desayunos', 2, 'Enrollado de huevo con vegetales', 'PERSONA', 40, NULL),
    ('Menu Desayunos', 3, 'Huevos revueltos con salsa ranchera', 'PERSONA', 40, NULL),
    ('Menu Desayunos', 4, 'Huevos revueltos con jamón', 'PERSONA', 40, NULL),

    ('Menu Boquitas', 1, 'Pinchos de pollo', 'CIENTO', 800, NULL),
    ('Menu Boquitas', 2, 'Pinchos de res', 'CIENTO', 1000, NULL),
    ('Menu Boquitas', 3, 'Alitas a la barbacoa', 'CIENTO', 600, NULL),
    ('Menu Boquitas', 4, 'Canapés de queso crema', 'CIENTO', 450, NULL),
    ('Menu Boquitas', 5, 'Canapés de jamón', 'CIENTO', 550, NULL),
    ('Menu Boquitas', 6, 'Bola de queso', 'UNIDAD', 75, NULL),
    ('Menu Boquitas', 7, 'Rollitos de canela', 'CIENTO', 500, NULL),
    ('Menu Boquitas', 8, 'Mini rellenitos', 'CIENTO', 500, NULL),
    ('Menu Boquitas', 9, 'Festival de mini donas con chocolate y anicillo', 'CIENTO', 350, NULL),
    ('Menu Boquitas', 10, 'Boquitas chapinas mixtas (mini tostadas, chuchitos, rellenitos y donas)', 'CIENTO', 500, NULL);

-- Menus y platos que falten, activos.
INSERT INTO menu (id_estado, nombre_menu)
SELECT (SELECT e.id_estado FROM estado e JOIN tipo_estado t ON t.id_tipo_estado = e.id_tipo_estado
        WHERE t.nombre_tipo = 'GENERAL' AND e.nombre = 'ACTIVO'),
       c.menu
FROM (SELECT DISTINCT menu FROM catalogo_2026) c
WHERE NOT EXISTS (SELECT 1 FROM menu m WHERE lower(btrim(m.nombre_menu)) = lower(c.menu));

INSERT INTO plato (id_estado, nombre_plato, unidad_venta)
SELECT (SELECT e.id_estado FROM estado e JOIN tipo_estado t ON t.id_tipo_estado = e.id_tipo_estado
        WHERE t.nombre_tipo = 'GENERAL' AND e.nombre = 'ACTIVO'),
       c.plato, c.unidad
FROM catalogo_2026 c
WHERE NOT EXISTS (SELECT 1 FROM plato p WHERE lower(btrim(p.nombre_plato)) = lower(c.plato));

UPDATE plato p
SET unidad_venta = c.unidad
FROM catalogo_2026 c
WHERE lower(btrim(p.nombre_plato)) = lower(c.plato);

-- Plato dentro de su menu, con sus dos precios. Si el enlace ya existia se
-- actualizan los precios y se respeta el orden que el usuario le hubiera dado.
-- Si hubiera nombres repetidos se usa el registro mas antiguo.
INSERT INTO menu_plato (id_menu, id_plato, orden_menu, precio_unitario, precio_desde_100)
SELECT m.id_menu, p.id_plato, c.orden, c.precio_base, c.precio_100
FROM catalogo_2026 c
JOIN LATERAL (SELECT id_menu FROM menu WHERE lower(btrim(nombre_menu)) = lower(c.menu)
              ORDER BY id_menu LIMIT 1) m ON TRUE
JOIN LATERAL (SELECT id_plato FROM plato WHERE lower(btrim(nombre_plato)) = lower(c.plato)
              ORDER BY id_plato LIMIT 1) p ON TRUE
ON CONFLICT (id_menu, id_plato) DO UPDATE
SET precio_unitario = EXCLUDED.precio_unitario,
    precio_desde_100 = EXCLUDED.precio_desde_100,
    fecha_modificacion = NOW();
