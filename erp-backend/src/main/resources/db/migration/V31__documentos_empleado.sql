-- ============================================================================
-- V31: Documentos de los empleados.
--
-- La tabla documento_empleado existia desde V1, pero el catalogo de tipos estaba vacio
-- y ninguna pantalla la usaba. Ahora:
--   - DPI: obligatorio para todo empleado (se pide en sus datos).
--   - Licencia de conducir: la exige el sistema para asignar a alguien como conductor
--     de un vehiculo en un evento.
-- El sistema reconoce estos dos tipos por su nombre, asi que no se pueden renombrar ni
-- borrar desde el catalogo; se pueden agregar otros tipos sin reglas especiales.
-- ============================================================================

INSERT INTO tipo_documento (nombre_tipo) VALUES
    ('DPI'),
    ('Licencia de conducir')
ON CONFLICT (nombre_tipo) DO NOTHING;

-- Un mismo numero de documento no puede pertenecer a dos empleados (dos personas con el
-- mismo DPI son en realidad la misma persona registrada dos veces).
CREATE UNIQUE INDEX uq_documento_empleado_tipo_numero
    ON documento_empleado (id_tipo_documento, numero_documento);
