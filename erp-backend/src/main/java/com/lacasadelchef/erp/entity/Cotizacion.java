package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cotizacion")
public class Cotizacion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cotizacion")
    private Integer idCotizacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_evento", nullable = false)
    private TipoEvento tipoEvento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ubicacion", nullable = false)
    private Ubicacion ubicacion;

    @Column(name = "cantidad_personas", nullable = false)
    private Integer cantidadPersonas;

    @Column(name = "fecha_cotizacion", nullable = false)
    private LocalDate fechaCotizacion = LocalDate.now();

    @Column(name = "fecha_evento")
    private LocalDate fechaEvento;

    @Column(name = "presupuesto_cliente", precision = 12, scale = 2)
    private BigDecimal presupuestoCliente;

    /** Inicio del servicio de 4 horas; el fin se calcula con CondicionesComerciales. */
    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    /** Bebida del menu que eligio el cliente (te frio, rosa de Jamaica...). */
    @Column(name = "bebida", length = 60)
    private String bebida;
}
