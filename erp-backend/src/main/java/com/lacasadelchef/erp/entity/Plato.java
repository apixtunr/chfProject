package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "plato")
public class Plato extends Auditable {

    private static final String SEPARADOR_BEBIDAS = "|";

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

    /**
     * Bebidas que incluye el plato, separadas por '|' ("Te frio|Rosa de Jamaica"); null =
     * no incluye bebida (boquitas). Se lee y escribe con opcionesBebida().
     */
    @Column(name = "bebidas", length = 120)
    private String bebidas;

    public List<String> opcionesBebida() {
        if (bebidas == null || bebidas.isBlank()) {
            return List.of();
        }
        return Arrays.stream(bebidas.split(Pattern.quote(SEPARADOR_BEBIDAS)))
                .map(String::trim)
                .filter(b -> !b.isEmpty())
                .toList();
    }

    public void setOpcionesBebida(List<String> opciones) {
        List<String> limpias = opciones == null ? List.of()
                : opciones.stream().filter(b -> b != null && !b.isBlank()).map(String::trim).distinct().toList();
        this.bebidas = limpias.isEmpty() ? null : String.join(SEPARADOR_BEBIDAS, limpias);
    }
}
