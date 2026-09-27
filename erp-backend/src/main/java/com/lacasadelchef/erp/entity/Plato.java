package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "plato")
public class Plato extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plato")
    private Integer idPlato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    @Column(name = "nombre_plato", nullable = false, length = 120)
    private String nombrePlato;

    /** Como se lee su precio en el menu: por persona, por ciento o por unidad. */
    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_venta", nullable = false, length = 10)
    private UnidadVenta unidadVenta = UnidadVenta.PERSONA;
}
