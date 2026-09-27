package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Bebida que puede incluir un plato del menu (te frio, rosa de Jamaica, atol...). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bebida")
public class Bebida extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bebida")
    private Integer idBebida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_estado", nullable = false)
    private Estado estado;

    @Column(name = "nombre_bebida", nullable = false, length = 80)
    private String nombreBebida;
}
