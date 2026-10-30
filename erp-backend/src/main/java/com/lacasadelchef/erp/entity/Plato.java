package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_venta", nullable = false, length = 10)
    private UnidadVenta unidadVenta = UnidadVenta.PERSONA;

    @OneToMany(mappedBy = "plato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlatoBebida> bebidas = new ArrayList<>();

    public List<Bebida> opcionesBebida() {
        return bebidas.stream()
                .map(PlatoBebida::getBebida)
                .filter(b -> b.getEstado() == null || "ACTIVO".equalsIgnoreCase(b.getEstado().getNombre()))
                .sorted(Comparator.comparing(Bebida::getIdBebida, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }
}
