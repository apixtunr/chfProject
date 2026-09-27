package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cotizacion_version")
public class CotizacionVersion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cotizacion_version")
    private Integer idCotizacionVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cotizacion", nullable = false)
    private Cotizacion cotizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    @Column(name = "numero_version", nullable = false)
    private Integer numeroVersion = 1;

    /** Mantenido por trigger en la base de datos a partir de los detalles. */
    @Column(name = "monto_total", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "fecha_version", insertable = false, updatable = false)
    private LocalDateTime fechaVersion;

    /** Cuando se envio al cliente; null mientras es borrador. */
    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    /** Ultimo dia en que el cliente puede aceptarla; despues pasa a VENCIDA. */
    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;
}
