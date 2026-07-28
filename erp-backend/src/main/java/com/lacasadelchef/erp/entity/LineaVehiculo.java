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
@Table(name = "linea_vehiculo")
public class LineaVehiculo extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_linea_vehiculo")
    private Integer idLineaVehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_marca_vehiculo", nullable = false)
    private MarcaVehiculo marcaVehiculo;

    @Column(name = "nombre_linea", nullable = false, length = 60)
    private String nombreLinea;
}
