-- ============================================================================
-- V21: Un usuario del sistema siempre es una persona del negocio.
--
-- La relacion entre empleado y usuario ya existia (usuario.id_empleado), pero
-- era muchos-a-uno y opcional de los dos lados. Eso permitia dos cosas que no
-- deberian pasar:
--
--   a) un usuario sin ninguna persona detras. Es el caso de 'admin': en la
--      bitacora, en los pagos y en los movimientos de inventario aparecia como
--      "admin", que no identifica a nadie. El proposito de una bitacora es
--      responder quien hizo que, y ahi no lo respondia.
--   b) varios usuarios apuntando al mismo empleado, porque id_empleado no tenia
--      indice unico.
--
-- Queda uno-a-uno con cardinalidad minima 0 del lado del empleado:
--
--     empleado  1 ──────── 0..1  usuario
--
--   - Un empleado tiene cero o un usuario. Cero es lo normal: de los 11
--     empleados actuales, 10 no entran al sistema (meseros, cocineros) pero si
--     se asignan a eventos. Obligarlos a tener usuario seria inventar
--     credenciales que nadie usa ni vigila.
--   - Un usuario pertenece a exactamente un empleado. La credencial no existe
--     sin la persona.
--
-- No se agregan columnas ni tablas: es la relacion que ya estaba, ajustada.
-- ============================================================================

-- 1. Puesto administrativo ----------------------------------------------------
-- Los 6 puestos del catalogo son operativos (Mesero, Cocinero, Chef, Bartender,
-- Supervisor de eventos, Ayudante de cocina). Quien administra el sistema
-- tambien es un empleado, y necesita un puesto donde encajar.
--
-- Ojo: el puesto (que hace la persona en el negocio) y el rol del sistema (que
-- puede hacer dentro de la aplicacion) son cosas distintas y no estan atadas.
-- Un empleado con puesto 'Administrador' podria tener rol OPERATIVO, y un Chef
-- podria tener rol ADMINISTRADOR. El puesto no influye en ningun permiso.
INSERT INTO puesto_empleado (nombre_rol)
VALUES ('Administrador')
ON CONFLICT (nombre_rol) DO NOTHING;

-- 2. La persona detras de 'admin' ---------------------------------------------
-- Se sigue la convencion de la tabla: nombre = nombres de pila, apellido = los
-- dos apellidos (como 'Ana Judith' / 'Lopez Caceres').
INSERT INTO empleado (id_puesto_empleado, id_estado, nombre, apellido)
SELECT p.id_puesto_empleado,
       (SELECT e.id_estado FROM estado e
        JOIN tipo_estado t ON t.id_tipo_estado = e.id_tipo_estado
        WHERE t.nombre_tipo = 'GENERAL' AND e.nombre = 'ACTIVO'),
       'Amado',
       'Soto Morales'
FROM puesto_empleado p
WHERE p.nombre_rol = 'Administrador'
  AND NOT EXISTS (
      SELECT 1 FROM empleado WHERE nombre = 'Amado' AND apellido = 'Soto Morales');

UPDATE usuario u
SET id_empleado = e.id_empleado
FROM empleado e
WHERE u.username = 'admin'
  AND u.id_empleado IS NULL
  AND e.nombre = 'Amado'
  AND e.apellido = 'Soto Morales';

-- 3. Cortar la migracion si quedo algun usuario sin persona --------------------
-- Preferible fallar aca, con los nombres en el log, que dejar pasar un ALTER que
-- de un error de restriccion sin decir de quien se trata.
DO $$
DECLARE
    huerfanos TEXT;
BEGIN
    SELECT string_agg(username, ', ') INTO huerfanos
    FROM usuario WHERE id_empleado IS NULL;

    IF huerfanos IS NOT NULL THEN
        RAISE EXCEPTION 'V21: estos usuarios no tienen empleado y hay que vincularlos antes de migrar: %', huerfanos;
    END IF;
END $$;

-- 4. Uno-a-uno ----------------------------------------------------------------
-- El indice unico baja el "varios" a "uno": un empleado no puede tener dos
-- usuarios.
CREATE UNIQUE INDEX IF NOT EXISTS uq_usuario_empleado ON usuario(id_empleado);

-- El NOT NULL quita el "cero" del lado del usuario: ningun usuario sin persona.
ALTER TABLE usuario ALTER COLUMN id_empleado SET NOT NULL;
