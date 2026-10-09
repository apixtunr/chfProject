-- ============================================================================
-- V32: Archivo de los comprobantes de pago.
--
-- comprobante_pago tenia archivo_url desde V1 pero nunca se llenaba: no habia donde
-- guardar el archivo. Ahora la foto de la boleta o el PDF de la transferencia se guarda
-- en su propia tabla, y archivo_url queda con la ruta de la API desde donde se descarga.
--
-- Va en una tabla aparte y no como columna de comprobante_pago para que al listar los
-- comprobantes de un pago no se traigan tambien los archivos, que pesan cientos de KB.
-- Al estar en la base, los respaldos diarios (pg_dump) ya los incluyen.
-- ============================================================================

CREATE TABLE comprobante_archivo (
    id_comprobante      INT PRIMARY KEY REFERENCES comprobante_pago(id_comprobante) ON DELETE CASCADE,
    nombre_archivo      VARCHAR(255) NOT NULL,
    tipo_contenido      VARCHAR(100) NOT NULL,
    tamano_bytes        INT NOT NULL,
    contenido           BYTEA NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_modificacion  TIMESTAMP,
    CONSTRAINT ck_comprobante_archivo_tipo
        CHECK (tipo_contenido IN ('application/pdf', 'image/jpeg', 'image/png')),
    CONSTRAINT ck_comprobante_archivo_tamano
        CHECK (tamano_bytes > 0 AND tamano_bytes <= 5242880)
);

-- El tipo de comprobante pasa de texto libre a una lista fija (no hay ninguno registrado).
ALTER TABLE comprobante_pago
    ADD CONSTRAINT ck_comprobante_pago_tipo
        CHECK (tipo_comprobante IN ('Boleta de depósito', 'Voucher de tarjeta',
                                    'Comprobante de transferencia', 'Factura', 'Recibo'));

-- Un mismo comprobante (tipo y numero) no puede respaldar dos pagos.
CREATE UNIQUE INDEX uq_comprobante_pago_tipo_numero
    ON comprobante_pago (tipo_comprobante, numero_comprobante);
