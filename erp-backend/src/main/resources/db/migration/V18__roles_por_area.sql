-- ============================================================================
-- V18: Roles por area de trabajo, con sus permisos.
--
-- Hasta ahora existian solo ADMINISTRADOR (que por diseno siempre tiene acceso
-- total, sin depender de rol_opcion) y OPERATIVO. Se agregan los cuatro roles
-- que corresponden a como se reparte el trabajo en el negocio, cada uno con
-- acceso unicamente a lo que necesita:
--
--   VENTAS   : atiende al cliente de principio a fin (cotiza, arma el evento y cobra)
--   COCINA   : mantiene el catalogo de menus y platos; consulta los eventos para saber que se sirve
--   BODEGA   : maneja el inventario; consulta los eventos para saber que insumos reservar
--   FINANZAS : cobros y rentabilidad
--
-- Sobre los permisos: una fila en rol_opcion con todas las banderas en FALSE
-- significa "solo consulta" (la pantalla se ve, pero no se puede modificar
-- nada). Si no existe la fila, PermisoService niega el acceso por completo.
-- Los permisos de impresion y exportacion se conceden donde hay un documento o
-- reporte que descargar (cotizaciones y rentabilidad).
-- Idempotente: ON CONFLICT DO NOTHING en ambas inserciones.
-- ============================================================================

INSERT INTO rol (nombre_rol) VALUES
 ('VENTAS'), ('COCINA'), ('BODEGA'), ('FINANZAS')
ON CONFLICT (nombre_rol) DO NOTHING;

INSERT INTO rol_opcion (id_rol, id_opcion, alta, baja, modificacion, imprimir, exportar)
SELECT r.id_rol, o.id_opcion, p.alta, p.baja, p.modificacion, p.imprimir, p.exportar
FROM (VALUES
    -- (rol,      pagina_url,                    alta,  baja,  modif, impr,  expor)
    ('VENTAS',   '/api/clientes',                TRUE,  TRUE,  TRUE,  FALSE, FALSE),
    ('VENTAS',   '/api/ubicaciones',             TRUE,  FALSE, TRUE,  FALSE, FALSE),
    ('VENTAS',   '/api/cotizaciones',            TRUE,  TRUE,  TRUE,  TRUE,  FALSE),
    ('VENTAS',   '/api/eventos',                 TRUE,  FALSE, TRUE,  FALSE, FALSE),
    ('VENTAS',   '/api/pagos',                   TRUE,  FALSE, TRUE,  FALSE, FALSE),
    ('VENTAS',   '/api/metodos-pago',            FALSE, FALSE, FALSE, FALSE, FALSE),
    ('VENTAS',   '/api/menus',                   FALSE, FALSE, FALSE, FALSE, FALSE),
    ('VENTAS',   '/api/platos',                  FALSE, FALSE, FALSE, FALSE, FALSE),

    ('COCINA',   '/api/menus',                   TRUE,  TRUE,  TRUE,  FALSE, FALSE),
    ('COCINA',   '/api/platos',                  TRUE,  TRUE,  TRUE,  FALSE, FALSE),
    ('COCINA',   '/api/eventos',                 FALSE, FALSE, FALSE, FALSE, FALSE),
    ('COCINA',   '/api/inventarios',             FALSE, FALSE, FALSE, FALSE, FALSE),

    ('BODEGA',   '/api/inventarios',             TRUE,  FALSE, TRUE,  FALSE, FALSE),
    ('BODEGA',   '/api/productos',               TRUE,  TRUE,  TRUE,  FALSE, FALSE),
    ('BODEGA',   '/api/movimientos-inventario',  TRUE,  FALSE, FALSE, FALSE, FALSE),
    ('BODEGA',   '/api/categorias-producto',     TRUE,  FALSE, TRUE,  FALSE, FALSE),
    ('BODEGA',   '/api/tipos-inventario',        FALSE, FALSE, FALSE, FALSE, FALSE),
    ('BODEGA',   '/api/eventos',                 FALSE, FALSE, FALSE, FALSE, FALSE),

    ('FINANZAS', '/api/pagos',                   TRUE,  FALSE, TRUE,  FALSE, FALSE),
    ('FINANZAS', '/api/metodos-pago',            TRUE,  FALSE, TRUE,  FALSE, FALSE),
    ('FINANZAS', '/api/rentabilidad',            FALSE, FALSE, FALSE, TRUE,  TRUE),
    ('FINANZAS', '/api/clientes',                FALSE, FALSE, FALSE, FALSE, FALSE),
    ('FINANZAS', '/api/eventos',                 FALSE, FALSE, FALSE, FALSE, FALSE),
    ('FINANZAS', '/api/cotizaciones',            FALSE, FALSE, FALSE, TRUE,  FALSE)
) AS p(rol, pagina_url, alta, baja, modificacion, imprimir, exportar)
JOIN rol r ON r.nombre_rol = p.rol
JOIN opcion o ON o.pagina_url = p.pagina_url
ON CONFLICT (id_rol, id_opcion) DO NOTHING;
