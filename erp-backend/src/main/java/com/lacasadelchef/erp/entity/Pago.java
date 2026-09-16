package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pago")
public class Pago extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evento", nullable = false)
    private Evento evento;

    /** Usuario que registro el pago. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    /** Si no es null, este pago es el reembolso de ese costo extra (no un abono al menu). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_costo_evento")
    private CostoEvento costoEvento;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "referencia_transaccion", length = 100)
    private String referenciaTransaccion;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago = LocalDateTime.now();
}
