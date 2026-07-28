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
@Table(name = "puesto_empleado")
public class PuestoEmpleado extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_puesto_empleado")
    private Integer idPuestoEmpleado;

    @Column(name = "nombre_rol", nullable = false, length = 80)
    private String nombreRol;
}
