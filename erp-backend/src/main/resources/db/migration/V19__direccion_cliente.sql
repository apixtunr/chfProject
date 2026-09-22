-- ============================================================================
-- V19: El cliente pasa a tener direccion propia, obligatoria.
--
-- Es distinta de ubicacion: ubicacion es el lugar donde se realiza el evento
-- (salones, fincas), y por eso en V11 se desligo del cliente. Esta es la
-- direccion del cliente en si, la que corresponde a su NIT para efectos de
-- facturacion.
--
-- Se guarda el municipio y no el departamento: el departamento se deduce del
-- municipio (municipio.id_departamento), asi que guardarlo tambien seria
-- repetir el dato y arriesgar que queden contradiciendose. La pantalla si
-- pide primero el departamento, pero solo para filtrar la lista de municipios.
--
-- Los 51 clientes que ya existian no tenian donde guardar una direccion, asi
-- que se les asigna una ficticia repartida entre los 22 departamentos del
-- pais, para que el sistema quede consistente y con datos de demostracion
-- variados. Son datos de prueba: conviene reemplazarlos con los reales.
-- ============================================================================

ALTER TABLE cliente ADD COLUMN direccion VARCHAR(255);
ALTER TABLE cliente ADD COLUMN id_municipio INT REFERENCES municipio(id_municipio);

-- Un municipio de referencia por departamento (el primero de cada uno), para
-- repartir los clientes existentes por todo el pais.
WITH muni_por_departamento AS (
    SELECT DISTINCT ON (m.id_departamento) m.id_departamento, m.id_municipio
    FROM municipio m
    ORDER BY m.id_departamento, m.id_municipio
),
referencia AS (
    SELECT id_municipio, (row_number() OVER (ORDER BY id_departamento) - 1) AS posicion
    FROM muni_por_departamento
),
pendientes AS (
    SELECT id_cliente, (row_number() OVER (ORDER BY id_cliente) - 1) AS posicion
    FROM cliente
    WHERE direccion IS NULL
),
total AS (SELECT count(*) AS n FROM referencia)
UPDATE cliente c
SET direccion = ((p.posicion % 9) + 1) || 'a calle ' ||
                ((p.posicion % 15) + 2) || '-' ||
                lpad((((p.posicion * 7) % 89) + 10)::text, 2, '0') ||
                ', zona ' || ((p.posicion % 12) + 1),
    id_municipio = r.id_municipio
FROM pendientes p
JOIN total t ON TRUE
JOIN referencia r ON r.posicion = p.posicion % t.n
WHERE c.id_cliente = p.id_cliente;

ALTER TABLE cliente ALTER COLUMN direccion SET NOT NULL;
ALTER TABLE cliente ALTER COLUMN id_municipio SET NOT NULL;

CREATE INDEX idx_cliente_municipio ON cliente(id_municipio);
