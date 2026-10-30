package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import com.lacasadelchef.erp.entity.id.PlatoBebidaId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "plato_bebida")
public class PlatoBebida extends Auditable {

    @EmbeddedId
    private PlatoBebidaId id;

    @MapsId("idPlato")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plato")
    private Plato plato;

    @MapsId("idBebida")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bebida")
    private Bebida bebida;

    public PlatoBebida(Plato plato, Bebida bebida) {
        this.id = new PlatoBebidaId(plato.getIdPlato(), bebida.getIdBebida());
        this.plato = plato;
        this.bebida = bebida;
    }
}
