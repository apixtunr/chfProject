package com.lacasadelchef.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Mapea la vista v_rentabilidad_evento (definida en el script SQL): ingresos, costos y
 * ganancia por evento, ya calculados en la base de datos. Es de solo lectura: no tiene
 * columnas de auditoria propias y @Immutable evita que Hibernate intente actualizarla.
 */
@Getter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "v_rentabilidad_evento")
public class VRentabilidadEvento {

    @Id
    @Column(name = "id_evento")
    private Integer idEvento;

    @Column(name = "total_ingresos", precision = 12, scale = 2)
    private BigDecimal totalIngresos;

    @Column(name = "total_costos", precision = 12, scale = 2)
    private BigDecimal totalCostos;

    @Column(name = "ganancia", precision = 12, scale = 2)
    private BigDecimal ganancia;

    @Column(name = "porcentaje", precision = 12, scale = 2)
    private BigDecimal porcentaje;
}
