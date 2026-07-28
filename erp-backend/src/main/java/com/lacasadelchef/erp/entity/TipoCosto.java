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
@Table(name = "tipo_costo")
public class TipoCosto extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_costo")
    private Integer idTipoCosto;

    @Column(name = "nombre_tipo", nullable = false, length = 80)
    private String nombreTipo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;
}
