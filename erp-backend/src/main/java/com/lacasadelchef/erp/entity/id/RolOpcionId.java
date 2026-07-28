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
public class RolOpcionId implements Serializable {

    @Column(name = "id_rol")
    private Integer idRol;

    @Column(name = "id_opcion")
    private Integer idOpcion;
}
