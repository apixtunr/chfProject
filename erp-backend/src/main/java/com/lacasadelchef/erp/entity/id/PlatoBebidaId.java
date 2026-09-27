package com.lacasadelchef.erp.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class PlatoBebidaId implements Serializable {

    @Column(name = "id_plato")
    private Integer idPlato;

    @Column(name = "id_bebida")
    private Integer idBebida;
}
