package com.lacasadelchef.erp.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Precio que se cobra por persona (o por unidad) segun el tamano del evento y como se vende el plato. */
class MenuPlatoTest {

    private static MenuPlato menuPlato(UnidadVenta unidad, String precioBase, String precioDesde100) {
        Plato plato = new Plato();
        plato.setUnidadVenta(unidad);
        MenuPlato menuPlato = new MenuPlato();
        menuPlato.setPlato(plato);
        menuPlato.setPrecioUnitario(new BigDecimal(precioBase));
        menuPlato.setPrecioDesde100(precioDesde100 == null ? null : new BigDecimal(precioDesde100));
        return menuPlato;
    }

    @Test
    @DisplayName("Lomo relleno: Q45 para menos de 100 personas y Q40 desde 100")
    void precioPorEscala() {
        MenuPlato lomo = menuPlato(UnidadVenta.PERSONA, "45.00", "40.00");

        assertThat(lomo.precioPorUnidadPara(50)).isEqualByComparingTo("45.00");
        assertThat(lomo.precioPorUnidadPara(99)).isEqualByComparingTo("45.00");
        assertThat(lomo.precioPorUnidadPara(100)).isEqualByComparingTo("40.00");
        assertThat(lomo.precioPorUnidadPara(250)).isEqualByComparingTo("40.00");
    }

    @Test
    @DisplayName("Un plato con un solo precio (menu de ninos) cobra lo mismo con cualquier cantidad de personas")
    void precioUnico() {
        MenuPlato hotDog = menuPlato(UnidadVenta.PERSONA, "30.00", null);

        assertThat(hotDog.precioPorUnidadPara(20)).isEqualByComparingTo("30.00");
        assertThat(hotDog.precioPorUnidadPara(300)).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("Boquitas por ciento: Q800 el ciento es Q8.00 por unidad, asi 150 pinchos cuestan Q1,200")
    void precioPorCiento() {
        MenuPlato pinchos = menuPlato(UnidadVenta.CIENTO, "800.00", null);

        BigDecimal porUnidad = pinchos.precioPorUnidadPara(80);

        assertThat(porUnidad).isEqualByComparingTo("8.00");
        assertThat(porUnidad.multiply(BigDecimal.valueOf(150))).isEqualByComparingTo("1200.00");
    }

    @Test
    @DisplayName("Venta por unidad: la bola de queso cuesta Q75 cada una")
    void precioPorUnidad() {
        MenuPlato bola = menuPlato(UnidadVenta.UNIDAD, "75.00", null);

        assertThat(bola.precioPorUnidadPara(120)).isEqualByComparingTo("75.00");
    }
}
