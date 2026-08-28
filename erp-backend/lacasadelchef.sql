-- ============================================================================
-- SISTEMA ERP - LA CASA DEL CHEF
-- Esquema de base de datos PostgreSQL (basado en DER v4 + ajustes acordados)
-- Motor: PostgreSQL 14+
-- ============================================================================
-- Ajustes aplicados sobre el DER v4:
--  1. evento_inventario y movimiento_inventario estandarizados a id_producto
--  2. Corregido typo fecha_crecion -> fecha_creacion (documento_empleado)
--  3. vehiculo con id_estado
--  4. movimiento_inventario con id_evento (nullable) e id_usuario
--  5. ubicacion.id_cliente nullable (salones propios o de terceros)
--  6. subtotal como columna generada; monto_total y cantidad_total
--     mantenidos por triggers
--  7. PKs compuestas en tablas puente
--  8. usuario.password almacena hash BCrypt (columna password_hash)
--  9. evento_vehiculo con id_empleado (conductor, nullable)
-- 10. evento admite id_cliente directo (sin cotizacion); CHECK garantiza origen
-- 11. usuario.tokens_validos_desde: revocacion de tokens JWT al cerrar sesion
-- 12. Seed de modulo/menu_vista/opcion (RBAC) para la matriz de permisos
-- ============================================================================

BEGIN;

-- ============================================================================
-- 1. ESTADOS GENERICOS
-- ============================================================================

CREATE TABLE tipo_estado (
    id_tipo_estado      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_tipo         VARCHAR(50) NOT NULL UNIQUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE estado (
    id_estado           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_tipo_estado      INT NOT NULL REFERENCES tipo_estado(id_tipo_estado),
    nombre              VARCHAR(50) NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    UNIQUE (id_tipo_estado, nombre)
);

-- ============================================================================
-- 2. GEOGRAFIA Y UBICACIONES
-- ============================================================================

CREATE TABLE departamento (
    id_departamento     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_departamento VARCHAR(100) NOT NULL UNIQUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE municipio (
    id_municipio        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_departamento     INT NOT NULL REFERENCES departamento(id_departamento),
    nombre_municipio    VARCHAR(100) NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    UNIQUE (id_departamento, nombre_municipio)
);

-- ============================================================================
-- 3. CLIENTES
-- ============================================================================

CREATE TABLE cliente (
    id_cliente          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre              VARCHAR(150) NOT NULL,
    correo              VARCHAR(150),
    telefono            VARCHAR(20),
    nit                 VARCHAR(20),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

-- id_cliente nullable: permite salones propios o de terceros
CREATE TABLE ubicacion (
    id_ubicacion        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cliente          INT REFERENCES cliente(id_cliente),
    id_municipio        INT NOT NULL REFERENCES municipio(id_municipio),
    direccion           VARCHAR(255) NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

-- ============================================================================
-- 4. EMPLEADOS
-- ============================================================================

CREATE TABLE genero (
    id_genero           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_genero       VARCHAR(30) NOT NULL UNIQUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE puesto_empleado (
    id_puesto_empleado  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_rol          VARCHAR(80) NOT NULL UNIQUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE empleado (
    id_empleado         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_puesto_empleado  INT NOT NULL REFERENCES puesto_empleado(id_puesto_empleado),
    id_estado           INT NOT NULL REFERENCES estado(id_estado),
    id_genero           INT REFERENCES genero(id_genero),
    nombre              VARCHAR(100) NOT NULL,
    apellido            VARCHAR(100) NOT NULL,
    correo              VARCHAR(150),
    telefono            VARCHAR(20),
    fecha_contratacion  DATE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE tipo_documento (
    id_tipo_documento   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_tipo         VARCHAR(50) NOT NULL UNIQUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE documento_empleado (
    id_empleado         INT NOT NULL REFERENCES empleado(id_empleado),
    id_tipo_documento   INT NOT NULL REFERENCES tipo_documento(id_tipo_documento),
    numero_documento    VARCHAR(50) NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    PRIMARY KEY (id_empleado, id_tipo_documento)
);

-- ============================================================================
-- 5. SEGURIDAD (usuarios, roles, opciones, bitacoras)
-- ============================================================================

CREATE TABLE rol (
    id_rol              INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_rol          VARCHAR(50) NOT NULL UNIQUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE usuario (
    id_usuario          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_estado           INT NOT NULL REFERENCES estado(id_estado),
    id_rol              INT NOT NULL REFERENCES rol(id_rol),
    id_empleado         INT REFERENCES empleado(id_empleado),
    username            VARCHAR(50) NOT NULL UNIQUE,
    password_hash       VARCHAR(100) NOT NULL, -- BCrypt via Spring Security
    intentos_acceso     INT NOT NULL DEFAULT 0,
    fecha_ultimo_acceso TIMESTAMP,
    -- Revocacion de tokens al cerrar sesion (JWT es stateless): al hacer logout se fija
    -- en NOW() y el filtro JWT rechaza cualquier token emitido antes. NULL = sin revocar.
    tokens_validos_desde TIMESTAMP,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE modulo (
    id_modulo           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre              VARCHAR(80) NOT NULL UNIQUE,
    orden               INT NOT NULL DEFAULT 0,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE menu_vista (
    id_menu_vista       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_modulo           INT NOT NULL REFERENCES modulo(id_modulo),
    nombre              VARCHAR(80) NOT NULL,
    orden               INT NOT NULL DEFAULT 0,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    UNIQUE (id_modulo, nombre)
);

CREATE TABLE opcion (
    id_opcion           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_menu_vista       INT NOT NULL REFERENCES menu_vista(id_menu_vista),
    nombre_opcion       VARCHAR(80) NOT NULL,
    orden_menu_vista    INT NOT NULL DEFAULT 0,
    pagina_url          VARCHAR(255) UNIQUE,
    accion              VARCHAR(80),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE rol_opcion (
    id_rol              INT NOT NULL REFERENCES rol(id_rol),
    id_opcion           INT NOT NULL REFERENCES opcion(id_opcion),
    alta                BOOLEAN NOT NULL DEFAULT FALSE,
    baja                BOOLEAN NOT NULL DEFAULT FALSE,
    modificacion        BOOLEAN NOT NULL DEFAULT FALSE,
    imprimir            BOOLEAN NOT NULL DEFAULT FALSE,
    exportar            BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    PRIMARY KEY (id_rol, id_opcion)
);

CREATE TABLE accion (
    id_accion           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre              VARCHAR(50) NOT NULL UNIQUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE bitacora_acceso (
    id_bitacora_acceso  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario          INT REFERENCES usuario(id_usuario),
    id_accion           INT NOT NULL REFERENCES accion(id_accion),
    fecha_acceso        TIMESTAMP NOT NULL DEFAULT NOW(),
    ip_origen           VARCHAR(45),
    navegador           VARCHAR(255),
    resultado           VARCHAR(50),
    sesion_id           VARCHAR(100)
);

CREATE TABLE bitacora_movimiento (
    id_bitacora_movimiento BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario          INT REFERENCES usuario(id_usuario),
    tabla_afectada      VARCHAR(80) NOT NULL,
    registro_id         VARCHAR(50),
    nombre_atributo     VARCHAR(80),
    valor_anterior      TEXT,
    valor_nuevo         TEXT,
    operacion           VARCHAR(20) NOT NULL, -- INSERT / UPDATE / DELETE
    ip_origen           VARCHAR(45),
    fecha_movimiento    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================================
-- 6. MENUS Y PLATOS
-- ============================================================================

CREATE TABLE plato (
    id_plato            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_estado           INT NOT NULL REFERENCES estado(id_estado),
    nombre_plato        VARCHAR(120) NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE menu (
    id_menu             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_estado           INT NOT NULL REFERENCES estado(id_estado),
    nombre_menu         VARCHAR(120) NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE menu_plato (
    id_menu             INT NOT NULL REFERENCES menu(id_menu),
    id_plato            INT NOT NULL REFERENCES plato(id_plato),
    orden_menu          INT NOT NULL DEFAULT 0,
    precio_unitario     NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    PRIMARY KEY (id_menu, id_plato)
);

-- ============================================================================
-- 7. COTIZACIONES (con versionado)
-- ============================================================================

CREATE TABLE tipo_evento (
    id_tipo_evento      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_tipo         VARCHAR(80) NOT NULL UNIQUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE cotizacion (
    id_cotizacion       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cliente          INT NOT NULL REFERENCES cliente(id_cliente),
    id_tipo_evento      INT NOT NULL REFERENCES tipo_evento(id_tipo_evento),
    id_ubicacion        INT NOT NULL REFERENCES ubicacion(id_ubicacion),
    cantidad_personas   INT NOT NULL CHECK (cantidad_personas > 0),
    fecha_cotizacion    DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_evento        DATE,
    presupuesto_cliente NUMERIC(12,2),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE cotizacion_version (
    id_cotizacion_version INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cotizacion       INT NOT NULL REFERENCES cotizacion(id_cotizacion),
    id_estado           INT NOT NULL REFERENCES estado(id_estado),
    numero_version      INT NOT NULL DEFAULT 1,
    monto_total         NUMERIC(12,2) NOT NULL DEFAULT 0, -- mantenido por trigger
    fecha_version       TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    UNIQUE (id_cotizacion, numero_version)
);

CREATE TABLE detalle_cotizacion (
    id_detalle_cotizacion INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cotizacion_version INT NOT NULL REFERENCES cotizacion_version(id_cotizacion_version),
    id_menu             INT NOT NULL REFERENCES menu(id_menu),
    id_plato            INT NOT NULL REFERENCES plato(id_plato),
    cantidad_platos     INT NOT NULL CHECK (cantidad_platos > 0),
    precio_unitario     NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal            NUMERIC(12,2) GENERATED ALWAYS AS (cantidad_platos * precio_unitario) STORED,
    observaciones       VARCHAR(255),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    FOREIGN KEY (id_menu, id_plato) REFERENCES menu_plato(id_menu, id_plato)
);

CREATE TABLE tipo_costo (
    id_tipo_costo       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_tipo         VARCHAR(80) NOT NULL UNIQUE,
    descripcion         VARCHAR(255),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

-- Catalogo de servicios extra no-menu que se cotizan al cliente (bebidas, decoracion,
-- personal...). Distinto de tipo_costo, que son gastos internos del negocio.
CREATE TABLE tipo_servicio (
    id_tipo_servicio    INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_tipo         VARCHAR(80) NOT NULL UNIQUE,
    descripcion         VARCHAR(255),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE servicio_cotizacion (
    id_servicio_cotizacion INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cotizacion_version  INT NOT NULL REFERENCES cotizacion_version(id_cotizacion_version),
    id_tipo_servicio       INT NOT NULL REFERENCES tipo_servicio(id_tipo_servicio),
    descripcion            VARCHAR(255),
    monto                  NUMERIC(12,2) NOT NULL CHECK (monto >= 0),
    fecha_creacion         TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion     TIMESTAMP
);

-- ============================================================================
-- 8. EVENTOS
-- ============================================================================

-- Ajuste 10: id_cotizacion_version es nullable y se agrega id_cliente para permitir
-- eventos directos (sin pasar por cotizacion). Si id_cotizacion_version esta presente,
-- el cliente se deriva de ahi (la app ignora id_cliente); si no, id_cliente es obligatorio.
-- Ademas de la validacion en la aplicacion, un CHECK garantiza a nivel de BD que todo
-- evento tenga origen (cotizacion o cliente), nunca ninguno de los dos.
CREATE TABLE evento (
    id_evento           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cotizacion_version INT REFERENCES cotizacion_version(id_cotizacion_version),
    id_cliente          INT REFERENCES cliente(id_cliente),
    id_tipo_evento      INT NOT NULL REFERENCES tipo_evento(id_tipo_evento),
    id_ubicacion        INT NOT NULL REFERENCES ubicacion(id_ubicacion),
    id_estado           INT NOT NULL REFERENCES estado(id_estado),
    fecha_evento        DATE NOT NULL,
    hora_inicio         TIME,
    hora_fin            TIME,
    cantidad_personas   INT CHECK (cantidad_personas > 0),
    observaciones       VARCHAR(500),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    monto_menu          NUMERIC(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT chk_evento_origen CHECK (id_cotizacion_version IS NOT NULL OR id_cliente IS NOT NULL)
);

CREATE TABLE detalle_evento (
    id_detalle_evento   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_evento           INT NOT NULL REFERENCES evento(id_evento),
    id_menu             INT NOT NULL REFERENCES menu(id_menu),
    id_plato            INT NOT NULL REFERENCES plato(id_plato),
    cantidad_platos     INT NOT NULL CHECK (cantidad_platos > 0),
    precio_unitario     NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal            NUMERIC(12,2) GENERATED ALWAYS AS (cantidad_platos * precio_unitario) STORED,
    observaciones       VARCHAR(255),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    FOREIGN KEY (id_menu, id_plato) REFERENCES menu_plato(id_menu, id_plato)
);

CREATE TABLE costo_evento (
    id_costo_evento     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_evento           INT NOT NULL REFERENCES evento(id_evento),
    id_tipo_costo       INT NOT NULL REFERENCES tipo_costo(id_tipo_costo),
    descripcion         VARCHAR(255),
    monto               NUMERIC(12,2) NOT NULL CHECK (monto >= 0),
    fecha_costo         DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE evento_empleado (
    id_evento           INT NOT NULL REFERENCES evento(id_evento),
    id_empleado         INT NOT NULL REFERENCES empleado(id_empleado),
    id_estado           INT NOT NULL REFERENCES estado(id_estado),
    salario_evento      NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (salario_evento >= 0),
    fecha_asignacion    TIMESTAMP NOT NULL DEFAULT NOW(),
    hora_inicio         TIME,
    hora_fin            TIME,
    PRIMARY KEY (id_evento, id_empleado)
);

-- ============================================================================
-- 9. VEHICULOS
-- ============================================================================

CREATE TABLE marca_vehiculo (
    id_marca_vehiculo   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_marca        VARCHAR(60) NOT NULL UNIQUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE linea_vehiculo (
    id_linea_vehiculo   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_marca_vehiculo   INT NOT NULL REFERENCES marca_vehiculo(id_marca_vehiculo),
    nombre_linea        VARCHAR(60) NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE tipo_placa (
    id_tipo_placa       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_tipo         VARCHAR(50) NOT NULL UNIQUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE vehiculo (
    id_vehiculo         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_linea_vehiculo   INT NOT NULL REFERENCES linea_vehiculo(id_linea_vehiculo),
    id_tipo_placa       INT NOT NULL REFERENCES tipo_placa(id_tipo_placa),
    id_estado           INT NOT NULL REFERENCES estado(id_estado), -- ajuste 3
    placa               VARCHAR(20) NOT NULL UNIQUE,
    anio_vehiculo       INT,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE evento_vehiculo (
    id_evento           INT NOT NULL REFERENCES evento(id_evento),
    id_vehiculo         INT NOT NULL REFERENCES vehiculo(id_vehiculo),
    id_empleado         INT REFERENCES empleado(id_empleado), -- conductor (ajuste 9)
    fecha_asignacion    TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id_evento, id_vehiculo)
);

-- ============================================================================
-- 10. INVENTARIO Y PRODUCTOS
-- ============================================================================

CREATE TABLE tipo_inventario (
    id_tipo_inventario  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_tipo         VARCHAR(80) NOT NULL UNIQUE,
    descripcion         VARCHAR(255),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE categoria_producto (
    id_categoria        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_tipo_inventario  INT NOT NULL REFERENCES tipo_inventario(id_tipo_inventario),
    nombre_categoria    VARCHAR(80) NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE producto (
    id_producto         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_categoria        INT NOT NULL REFERENCES categoria_producto(id_categoria),
    nombre_producto     VARCHAR(120) NOT NULL,
    unidad_medida       VARCHAR(30) NOT NULL,
    precio_unitario     NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (precio_unitario >= 0),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

-- 1:1 con producto (stock actual)
CREATE TABLE inventario (
    id_inventario       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_producto         INT NOT NULL UNIQUE REFERENCES producto(id_producto),
    cantidad_total      NUMERIC(12,2) NOT NULL DEFAULT 0, -- mantenido por trigger
    cantidad_minima     NUMERIC(12,2) NOT NULL DEFAULT 0,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

-- Ajuste 1: referencia a producto. Ajuste 4: trazabilidad evento/usuario.
CREATE TABLE movimiento_inventario (
    id_movimiento       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_producto         INT NOT NULL REFERENCES producto(id_producto),
    id_evento           INT REFERENCES evento(id_evento),
    id_usuario          INT REFERENCES usuario(id_usuario),
    tipo_movimiento     VARCHAR(20) NOT NULL CHECK (tipo_movimiento IN ('ENTRADA','SALIDA','AJUSTE')),
    cantidad            NUMERIC(12,2) NOT NULL CHECK (cantidad <> 0),
    descripcion         VARCHAR(255),
    fecha_movimiento    TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE evento_inventario (
    id_evento           INT NOT NULL REFERENCES evento(id_evento),
    id_producto         INT NOT NULL REFERENCES producto(id_producto), -- ajuste 1
    cantidad            NUMERIC(12,2) NOT NULL CHECK (cantidad > 0),
    fecha_consumo       TIMESTAMP,
    PRIMARY KEY (id_evento, id_producto)
);

-- ============================================================================
-- 11. PAGOS
-- ============================================================================

CREATE TABLE metodo_pago (
    id_metodo_pago      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_estado           INT NOT NULL REFERENCES estado(id_estado),
    nombre_metodo       VARCHAR(60) NOT NULL UNIQUE,
    descripcion         VARCHAR(255),
    requiere_referencia BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE pago (
    id_pago             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_evento           INT NOT NULL REFERENCES evento(id_evento),
    id_usuario          INT NOT NULL REFERENCES usuario(id_usuario),
    id_metodo_pago      INT NOT NULL REFERENCES metodo_pago(id_metodo_pago),
    id_estado           INT NOT NULL REFERENCES estado(id_estado),
    monto               NUMERIC(12,2) NOT NULL CHECK (monto > 0),
    referencia_transaccion VARCHAR(100),
    observaciones       VARCHAR(255),
    fecha_pago          TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

CREATE TABLE comprobante_pago (
    id_comprobante      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_pago             INT NOT NULL REFERENCES pago(id_pago),
    numero_comprobante  VARCHAR(50) NOT NULL,
    archivo_url         VARCHAR(500),
    tipo_comprobante    VARCHAR(50),
    fecha_emision       DATE,
    es_valido           BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP
);

-- ============================================================================
-- 12. TRIGGERS
-- ============================================================================

-- 12.1 fecha_modificacion automatica en todo UPDATE
CREATE OR REPLACE FUNCTION fn_set_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_modificacion := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE t RECORD;
BEGIN
    FOR t IN
        SELECT table_name FROM information_schema.columns
        WHERE table_schema = 'public' AND column_name = 'fecha_modificacion'
    LOOP
        EXECUTE format(
            'CREATE TRIGGER trg_%s_fecha_mod BEFORE UPDATE ON %I
             FOR EACH ROW EXECUTE FUNCTION fn_set_fecha_modificacion();',
            t.table_name, t.table_name);
    END LOOP;
END $$;

-- 12.2 stock: mantiene inventario.cantidad_total desde movimiento_inventario
CREATE OR REPLACE FUNCTION fn_actualizar_stock()
RETURNS TRIGGER AS $$
DECLARE delta NUMERIC(12,2);
BEGIN
    delta := CASE NEW.tipo_movimiento
                WHEN 'ENTRADA' THEN NEW.cantidad
                WHEN 'SALIDA'  THEN -NEW.cantidad
                ELSE NEW.cantidad -- AJUSTE: cantidad con signo
             END;
    UPDATE inventario
       SET cantidad_total = cantidad_total + delta
     WHERE id_producto = NEW.id_producto;
    IF NOT FOUND THEN
        INSERT INTO inventario (id_producto, cantidad_total) VALUES (NEW.id_producto, delta);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_movimiento_stock
AFTER INSERT ON movimiento_inventario
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_stock();

-- 12.3 monto_total de la version de cotizacion: suma el detalle de menu/plato
-- y los servicios extra no-menu (bebidas, decoracion, personal, etc.)
CREATE OR REPLACE FUNCTION fn_actualizar_monto_version()
RETURNS TRIGGER AS $$
DECLARE v_id INT;
BEGIN
    v_id := COALESCE(NEW.id_cotizacion_version, OLD.id_cotizacion_version);
    UPDATE cotizacion_version
       SET monto_total = COALESCE((
            SELECT SUM(subtotal) FROM detalle_cotizacion
            WHERE id_cotizacion_version = v_id), 0)
          + COALESCE((
            SELECT SUM(monto) FROM servicio_cotizacion
            WHERE id_cotizacion_version = v_id), 0)
     WHERE id_cotizacion_version = v_id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_detalle_monto_total
AFTER INSERT OR UPDATE OR DELETE ON detalle_cotizacion
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_monto_version();

CREATE TRIGGER trg_servicio_cotizacion_monto_total
AFTER INSERT OR UPDATE OR DELETE ON servicio_cotizacion
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_monto_version();

CREATE OR REPLACE FUNCTION fn_actualizar_monto_evento()
RETURNS TRIGGER AS $$
DECLARE v_id INT;
BEGIN
    v_id := COALESCE(NEW.id_evento, OLD.id_evento);
    UPDATE evento
       SET monto_menu = COALESCE((
            SELECT SUM(subtotal) FROM detalle_evento
            WHERE id_evento = v_id), 0)
     WHERE id_evento = v_id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_detalle_evento_monto_total
AFTER INSERT OR UPDATE OR DELETE ON detalle_evento
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_monto_evento();

-- ============================================================================
-- 13. VISTAS FINANCIERAS (datos derivados: no se almacenan)
-- ============================================================================

CREATE VIEW v_ingresos_evento AS
SELECT e.id_evento,
       COALESCE(SUM(p.monto), 0) AS total_ingresos
FROM evento e
LEFT JOIN pago p ON p.id_evento = e.id_evento
GROUP BY e.id_evento;

CREATE VIEW v_costos_evento AS
SELECT e.id_evento,
       COALESCE(SUM(c.monto), 0)
         + COALESCE((SELECT SUM(ee.salario_evento) FROM evento_empleado ee
                     WHERE ee.id_evento = e.id_evento), 0) AS total_costos
FROM evento e
LEFT JOIN costo_evento c ON c.id_evento = e.id_evento
GROUP BY e.id_evento;

CREATE VIEW v_rentabilidad_evento AS
SELECT i.id_evento,
       i.total_ingresos,
       c.total_costos,
       i.total_ingresos - c.total_costos AS ganancia,
       CASE WHEN i.total_ingresos > 0
            THEN ROUND((i.total_ingresos - c.total_costos) / i.total_ingresos * 100, 2)
            ELSE 0 END AS porcentaje
FROM v_ingresos_evento i
JOIN v_costos_evento c USING (id_evento);

-- ============================================================================
-- 14. INDICES para llaves foraneas mas consultadas
-- ============================================================================

CREATE INDEX idx_cotizacion_cliente        ON cotizacion(id_cliente);
CREATE INDEX idx_cot_version_cotizacion    ON cotizacion_version(id_cotizacion);
CREATE INDEX idx_detalle_cot_version       ON detalle_cotizacion(id_cotizacion_version);
CREATE INDEX idx_detalle_evento_evento     ON detalle_evento(id_evento);
CREATE INDEX idx_evento_cot_version        ON evento(id_cotizacion_version);
CREATE INDEX idx_evento_cliente            ON evento(id_cliente);
CREATE INDEX idx_evento_fecha              ON evento(fecha_evento);
CREATE INDEX idx_costo_evento_evento       ON costo_evento(id_evento);
CREATE INDEX idx_pago_evento               ON pago(id_evento);
CREATE INDEX idx_mov_inv_producto          ON movimiento_inventario(id_producto);
CREATE INDEX idx_mov_inv_evento            ON movimiento_inventario(id_evento);
CREATE INDEX idx_bitacora_mov_usuario      ON bitacora_movimiento(id_usuario);
CREATE INDEX idx_bitacora_acc_usuario      ON bitacora_acceso(id_usuario);

-- ============================================================================
-- 15. DATOS SEMILLA MINIMOS
-- ============================================================================

INSERT INTO tipo_estado (nombre_tipo) VALUES
 ('GENERAL'), ('COTIZACION'), ('EVENTO'), ('PAGO'), ('VEHICULO');

INSERT INTO estado (id_tipo_estado, nombre) VALUES
 (1, 'ACTIVO'), (1, 'INACTIVO'),
 (2, 'CREADA'), (2, 'ENVIADA'), (2, 'ACEPTADA'), (2, 'RECHAZADA'),
 (3, 'PLANIFICADO'), (3, 'EN CURSO'), (3, 'FINALIZADO'), (3, 'CANCELADO'),
 (4, 'PENDIENTE'), (4, 'CONFIRMADO'), (4, 'ANULADO'),
 (5, 'DISPONIBLE'), (5, 'EN MANTENIMIENTO');

INSERT INTO accion (nombre) VALUES
 ('LOGIN'), ('LOGOUT'), ('LOGIN_FALLIDO');

INSERT INTO rol (nombre_rol) VALUES
 ('ADMINISTRADOR'), ('OPERATIVO');

-- Estructura de seguridad (modulo -> menu_vista -> opcion) con todas las pantallas
-- del frontend, para poder configurar permisos de roles desde la matriz de
-- administracion. pagina_url debe coincidir EXACTAMENTE con lo que usan
-- PermisoService (backend) y AuthService/nav-items (frontend).
INSERT INTO modulo (nombre, orden) VALUES
 ('Clientes', 1),
 ('Cotizaciones', 2),
 ('Eventos', 3),
 ('Inventarios', 4),
 ('Menus y platos', 5),
 ('Pagos', 6),
 ('Rentabilidad', 7),
 ('Administracion', 8);

-- Un menu_vista principal por modulo, mas "Catalogos" bajo Administracion
INSERT INTO menu_vista (id_modulo, nombre, orden)
SELECT id_modulo, nombre, orden FROM modulo;

INSERT INTO menu_vista (id_modulo, nombre, orden)
SELECT id_modulo, 'Catalogos', 10 FROM modulo WHERE nombre = 'Administracion';

-- Opciones (pantallas). accion documenta el CRUD disponible.
INSERT INTO opcion (id_menu_vista, nombre_opcion, orden_menu_vista, pagina_url, accion)
SELECT mv.id_menu_vista, o.nombre_opcion, o.orden, o.pagina_url, 'CRUD'
FROM (VALUES
    -- (modulo,            nombre_opcion,               orden, pagina_url)
    ('Clientes',        'Clientes',                  1, '/api/clientes'),
    ('Clientes',        'Ubicaciones',               2, '/api/ubicaciones'),
    ('Cotizaciones',    'Cotizaciones',              1, '/api/cotizaciones'),
    ('Eventos',         'Eventos',                   1, '/api/eventos'),
    ('Inventarios',     'Stock de inventario',       1, '/api/inventarios'),
    ('Inventarios',     'Productos',                 2, '/api/productos'),
    ('Inventarios',     'Movimientos de inventario', 3, '/api/movimientos-inventario'),
    ('Menus y platos',  'Menus',                     1, '/api/menus'),
    ('Menus y platos',  'Platos',                    2, '/api/platos'),
    ('Pagos',           'Pagos',                     1, '/api/pagos'),
    ('Pagos',           'Metodos de pago',           2, '/api/metodos-pago'),
    ('Rentabilidad',    'Reporte de rentabilidad',   1, '/api/rentabilidad'),
    ('Administracion',  'Usuarios',                  1, '/api/usuarios'),
    ('Administracion',  'Roles y permisos',          2, '/api/roles'),
    ('Administracion',  'Opciones del sistema',      3, '/api/opciones'),
    ('Administracion',  'Empleados',                 4, '/api/empleados'),
    ('Administracion',  'Vehiculos',                 5, '/api/vehiculos')
) AS o(modulo, nombre_opcion, orden, pagina_url)
JOIN modulo m ON m.nombre = o.modulo
JOIN menu_vista mv ON mv.id_modulo = m.id_modulo AND mv.nombre = m.nombre;

-- Catalogos simples, registrados como opciones bajo el menu_vista "Catalogos"
-- de Administracion, para poder asignarles permisos por rol desde la matriz.
INSERT INTO opcion (id_menu_vista, nombre_opcion, orden_menu_vista, pagina_url, accion)
SELECT mv.id_menu_vista, o.nombre_opcion, o.orden, o.pagina_url, 'CRUD'
FROM (VALUES
    ('Departamentos',            1,  '/api/departamentos'),
    ('Municipios',               2,  '/api/municipios'),
    ('Tipos de estado',          3,  '/api/tipos-estado'),
    ('Estados',                  4,  '/api/estados'),
    ('Generos',                  5,  '/api/generos'),
    ('Tipos de documento',       6,  '/api/tipos-documento'),
    ('Tipos de evento',          7,  '/api/tipos-evento'),
    ('Tipos de costo',           8,  '/api/tipos-costo'),
    ('Puestos de empleado',      9,  '/api/puestos-empleado'),
    ('Marcas de vehiculo',       10, '/api/marcas-vehiculo'),
    ('Lineas de vehiculo',       11, '/api/lineas-vehiculo'),
    ('Tipos de placa',           12, '/api/tipos-placa'),
    ('Tipos de inventario',      13, '/api/tipos-inventario'),
    ('Categorias de producto',   14, '/api/categorias-producto'),
    ('Tipos de servicio',        15, '/api/tipos-servicio')
) AS o(nombre_opcion, orden, pagina_url)
JOIN modulo m ON m.nombre = 'Administracion'
JOIN menu_vista mv ON mv.id_modulo = m.id_modulo AND mv.nombre = 'Catalogos';

COMMIT;
