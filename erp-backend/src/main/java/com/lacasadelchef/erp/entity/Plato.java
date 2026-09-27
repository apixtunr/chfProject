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

    /** Como se lee su precio en el menu: por persona, por ciento o por unidad. */
    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_venta", nullable = false, length = 10)
    private UnidadVenta unidadVenta = UnidadVenta.PERSONA;

    /** Bebidas que incluye el plato; vacio = no incluye bebida (boquitas). */
    @OneToMany(mappedBy = "plato", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlatoBebida> bebidas = new ArrayList<>();

    /** Las bebidas que se pueden elegir al cotizar el plato: las activas, en orden de catalogo. */
    public List<Bebida> opcionesBebida() {
        return bebidas.stream()
                .map(PlatoBebida::getBebida)
                .filter(b -> b.getEstado() == null || "ACTIVO".equalsIgnoreCase(b.getEstado().getNombre()))
                .sorted(Comparator.comparing(Bebida::getIdBebida, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }
}
