package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Bebida;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Plato;
import com.lacasadelchef.erp.entity.PlatoBebida;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BebidaDelPlatoTest {

    public static final Bebida TE_FRIO = bebida(1, "Té frío", "ACTIVO");
    public static final Bebida JAMAICA = bebida(2, "Rosa de Jamaica", "ACTIVO");
    public static final Bebida JUGO_Y_CAFE = bebida(4, "Jugo de naranja y estación de café", "ACTIVO");

    static Bebida bebida(int id, String nombre, String estadoNombre) {
        Estado estado = new Estado();
        estado.setNombre(estadoNombre);
        Bebida bebida = new Bebida();
        bebida.setIdBebida(id);
        bebida.setNombreBebida(nombre);
        bebida.setEstado(estado);
        return bebida;
    }

    /** Plato con sus filas de plato_bebida. */
    static Plato plato(String nombre, Bebida... bebidas) {
        Plato plato = new Plato();
        plato.setIdPlato(nombre.hashCode());
        plato.setNombrePlato(nombre);
        for (Bebida bebida : bebidas) {
            plato.getBebidas().add(new PlatoBebida(plato, bebida));
        }
        return plato;
    }

    @Test
    @DisplayName("Se guarda la bebida elegida si es de las que incluye el plato")
    void eligeDeLasDelPlato() {
        Plato lomo = plato("Lomo relleno", TE_FRIO, JAMAICA);

        assertThat(BebidaDelPlato.elegir(lomo, 2, null)).isSameAs(JAMAICA);
    }

    @Test
    @DisplayName("Una bebida que el plato no incluye se rechaza diciendo cuales incluye")
    void rechazaOtraBebida() {
        Plato milanesa = plato("Milanesa de pollo gratinada con queso", TE_FRIO);

        assertThatThrownBy(() -> BebidaDelPlato.elegir(milanesa, JAMAICA.getIdBebida(), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("incluye Té frío");
    }

    @Test
    @DisplayName("Si el plato incluye una sola bebida, se pone sola")
    void unicaSePoneSola() {
        Plato desayuno = plato("Huevos revueltos con jamón", JUGO_Y_CAFE);

        assertThat(BebidaDelPlato.elegir(desayuno, null, null)).isSameAs(JUGO_Y_CAFE);
    }

    @Test
    @DisplayName("Con varias bebidas y ninguna elegida queda pendiente; sin bebida en el plato, nunca lleva")
    void pendienteOSinBebida() {
        assertThat(BebidaDelPlato.elegir(plato("Lomo relleno", TE_FRIO, JAMAICA), null, null)).isNull();
        assertThat(BebidaDelPlato.elegir(plato("Pinchos de pollo"), 1, null)).isNull();
    }

    @Test
    @DisplayName("Una bebida inactiva ya no se ofrece, pero la linea que ya la tenia la conserva")
    void inactivaSeConservaEnLaLinea() {
        Bebida horchata = bebida(9, "Horchata", "INACTIVO");
        Plato lomo = plato("Lomo relleno", TE_FRIO, horchata);

        assertThat(lomo.opcionesBebida()).containsExactly(TE_FRIO);
        assertThat(BebidaDelPlato.elegir(lomo, 9, horchata)).isSameAs(horchata);
        assertThatThrownBy(() -> BebidaDelPlato.elegir(lomo, 9, null)).isInstanceOf(BusinessException.class);
    }
}
