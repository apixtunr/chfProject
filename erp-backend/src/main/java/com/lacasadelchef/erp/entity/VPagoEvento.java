package com.lacasadelchef.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mapea la vista v_pago_evento (definida en el script SQL): total, abonado y pendiente por
 * evento, ya calculados en la base de datos. Es de solo lectura: no tiene columnas de
 * auditoria propias y @Immutable evita que Hibernate intente actualizarla.
 */
@Getter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "v_pago_evento")
public class VPagoEvento {

    @Id
    @Column(name = "id_evento")
    private Integer idEvento;

    @Column(name = "fecha_evento")
    private LocalDate fechaEvento;

    @Column(name = "id_estado")
    private Integer idEstado;

    @Column(name = "estado_nombre")
    private String estadoNombre;

    @Column(name = "tipo_evento_nombre")
    private String tipoEventoNombre;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "cliente_nombre")
    private String clienteNombre;

    @Column(name = "total", precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "abonado", precision = 12, scale = 2)
    private BigDecimal abonado;

    @Column(name = "pendiente", precision = 12, scale = 2)
    private BigDecimal pendiente;
}
