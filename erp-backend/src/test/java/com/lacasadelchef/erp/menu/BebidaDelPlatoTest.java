package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Plato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BebidaDelPlatoTest {

    private static Plato plato(String nombre, String... bebidas) {
        Plato plato = new Plato();
        plato.setNombrePlato(nombre);
        plato.setOpcionesBebida(List.of(bebidas));
        return plato;
    }

    @Test
    @DisplayName("Se guarda la bebida elegida si es de las que incluye el plato")
    void eligeDeLasDelPlato() {
        Plato lomo = plato("Lomo relleno", "Té frío", "Rosa de Jamaica");

        assertThat(BebidaDelPlato.elegir(lomo, "rosa de jamaica")).isEqualTo("Rosa de Jamaica");
    }

    @Test
    @DisplayName("Una bebida que el plato no incluye se rechaza diciendo cuales incluye")
    void rechazaOtraBebida() {
        Plato milanesa = plato("Milanesa de pollo gratinada con queso", "Té frío");

        assertThatThrownBy(() -> BebidaDelPlato.elegir(milanesa, "Rosa de Jamaica"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("incluye Té frío");
    }

    @Test
    @DisplayName("Si el plato incluye una sola bebida, se pone sola")
    void unicaSePoneSola() {
        Plato desayuno = plato("Huevos revueltos con jamón", "Jugo de naranja y café");

        assertThat(BebidaDelPlato.elegir(desayuno, null)).isEqualTo("Jugo de naranja y café");
    }

    @Test
    @DisplayName("Con varias bebidas y ninguna elegida queda pendiente; sin bebida en el plato, nunca lleva")
    void pendienteOSinBebida() {
        assertThat(BebidaDelPlato.elegir(plato("Lomo relleno", "Té frío", "Rosa de Jamaica"), " ")).isNull();
        assertThat(BebidaDelPlato.elegir(plato("Pinchos de pollo"), "Té frío")).isNull();
    }
}
