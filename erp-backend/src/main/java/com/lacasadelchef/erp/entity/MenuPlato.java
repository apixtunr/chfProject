package com.lacasadelchef.erp.entity;

import com.lacasadelchef.erp.common.audit.Auditable;
import com.lacasadelchef.erp.entity.id.MenuPlatoId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "menu_plato")
public class MenuPlato extends Auditable {

    /** Desde cuantas personas en el evento aplica precioDesde100. */
    public static final int PERSONAS_PRECIO_VOLUMEN = 100;

    @EmbeddedId
    private MenuPlatoId id;

    @MapsId("idMenu")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_menu")
    private Menu menu;

    @MapsId("idPlato")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plato")
    private Plato plato;

    @Column(name = "orden_menu", nullable = false)
    private Integer ordenMenu = 0;

    /** Precio base: eventos de menos de 100 personas, o unico precio si no hay de volumen. */
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    /** Precio para eventos desde 100 personas; null = se cobra el precio base. */
    @Column(name = "precio_desde_100", precision = 12, scale = 2)
    private BigDecimal precioDesde100;

    /**
     * Precio por persona (o por unidad) para un evento de tantas personas: la escala la
     * decide el total de invitados, no la cantidad de este plato, y el precio de
     * catalogo se lleva a una sola unidad (el ciento de Q800.00 da Q8.00).
     */
    public BigDecimal precioPorUnidadPara(int personasEvento) {
        BigDecimal precio = precioDesde100 != null && personasEvento >= PERSONAS_PRECIO_VOLUMEN
                ? precioDesde100
                : precioUnitario;
        return plato.getUnidadVenta().precioPorUnidad(precio);
    }
}
