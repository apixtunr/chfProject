package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "inventario")
public class Inventario extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Integer idInventario;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    /**
     * Mantenido por trigger a partir de movimiento_inventario; solo lectura.
     * @Generated(INSERT) solo cubre el valor inicial (DEFAULT 0) de esta misma fila;
     * cuando el trigger la actualiza desde otra tabla hay que refrescar explicitamente.
     */
    @Generated(event = EventType.INSERT)
    @Column(name = "cantidad_total", insertable = false, updatable = false, precision = 12, scale = 2)
    private BigDecimal cantidadTotal;

    @Column(name = "cantidad_minima", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidadMinima = BigDecimal.ZERO;
}
