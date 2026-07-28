package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import com.lacasadelchef.erp.entity.id.RolOpcionId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "rol_opcion")
public class RolOpcion extends Auditable {

    @EmbeddedId
    private RolOpcionId id;

    @MapsId("idRol")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @MapsId("idOpcion")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_opcion")
    private Opcion opcion;

    @Column(name = "alta", nullable = false)
    private boolean alta;

    @Column(name = "baja", nullable = false)
    private boolean baja;

    @Column(name = "modificacion", nullable = false)
    private boolean modificacion;

    @Column(name = "imprimir", nullable = false)
    private boolean imprimir;

    @Column(name = "exportar", nullable = false)
    private boolean exportar;
}
