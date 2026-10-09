package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * El archivo de un comprobante de pago (foto de la boleta o PDF de la transferencia).
 *
 * Va aparte de ComprobantePago para que listar los comprobantes no cargue los archivos.
 * Comparte la llave con su comprobante: un comprobante tiene a lo sumo un archivo.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comprobante_archivo")
public class ComprobanteArchivo extends Auditable {

    @Id
    @Column(name = "id_comprobante")
    private Integer idComprobante;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_comprobante")
    private ComprobantePago comprobante;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "tipo_contenido", nullable = false, length = 100)
    private String tipoContenido;

    @Column(name = "tamano_bytes", nullable = false)
    private int tamanoBytes;

    @Column(name = "contenido", nullable = false)
    private byte[] contenido;
}
