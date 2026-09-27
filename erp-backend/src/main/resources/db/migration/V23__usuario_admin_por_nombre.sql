-- ============================================================================
-- V23: 'admin' pasa a llamarse como la persona que lo usa.
--
-- La empresa nombra a sus usuarios con primer nombre, punto y primer apellido,
-- en minusculas y sin tildes, igual que los correos (ana.lopez). 'admin' era
-- la unica cuenta generica: en la bitacora y en las pantallas no decia quien
-- era. Desde V21 pertenece a Amado Soto Morales, asi que pasa a 'amado.soto'
-- (o 'amado.soto2'... si ya existiera). La contrasena no cambia.
--
-- Los usuarios nuevos ya los genera el sistema con esta regla
-- (PoliticaUsuario); esta migracion solo corrige la cuenta que ya existia.
-- ============================================================================

DO $$
DECLARE
    v_id_usuario INT;
    v_base       TEXT;
    v_nuevo      TEXT;
    v_numero     INT := 1;
BEGIN
    SELECT u.id_usuario,
           translate(lower(split_part(btrim(e.nombre), ' ', 1)), 'áéíóúüñ', 'aeiouun') || '.' ||
           translate(lower(split_part(btrim(e.apellido), ' ', 1)), 'áéíóúüñ', 'aeiouun')
    INTO v_id_usuario, v_base
    FROM usuario u
    JOIN empleado e ON e.id_empleado = u.id_empleado
    WHERE u.username = 'admin';

    IF v_id_usuario IS NULL THEN
        RETURN; -- base sin la cuenta 'admin' (instalacion nueva): nada que corregir
    END IF;

    v_nuevo := v_base;
    WHILE EXISTS (SELECT 1 FROM usuario WHERE lower(username) = v_nuevo AND id_usuario <> v_id_usuario) LOOP
        v_numero := v_numero + 1;
        v_nuevo := v_base || v_numero;
    END LOOP;

    UPDATE usuario SET username = v_nuevo, fecha_modificacion = NOW() WHERE id_usuario = v_id_usuario;
    RAISE NOTICE 'V23: el usuario admin ahora se llama %', v_nuevo;
END $$;
