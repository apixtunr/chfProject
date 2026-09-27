package com.lacasadelchef.erp.cotizacion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Horario del servicio segun el menu de la empresa. */
class CondicionesComercialesTest {

    @Test
    @DisplayName("El servicio empieza en punto entre las 11:00 y las 19:00")
    void turnosDeInicio() {
        assertThat(CondicionesComerciales.esHoraDeInicioPermitida(LocalTime.of(11, 0))).isTrue();
        assertThat(CondicionesComerciales.esHoraDeInicioPermitida(LocalTime.of(19, 0))).isTrue();
        assertThat(CondicionesComerciales.esHoraDeInicioPermitida(LocalTime.of(10, 0))).isFalse();
        assertThat(CondicionesComerciales.esHoraDeInicioPermitida(LocalTime.of(20, 0))).isFalse();
        assertThat(CondicionesComerciales.esHoraDeInicioPermitida(LocalTime.of(13, 30))).isFalse();
    }

    @Test
    @DisplayName("Dura 4 horas y termina a las 21:00, o a las 22:00 si inicia a las 18 o 19")
    void horaDeFin() {
        assertThat(CondicionesComerciales.horaFinServicio(LocalTime.of(11, 0))).isEqualTo(LocalTime.of(15, 0));
        assertThat(CondicionesComerciales.horaFinServicio(LocalTime.of(17, 0))).isEqualTo(LocalTime.of(21, 0));
        assertThat(CondicionesComerciales.horaFinServicio(LocalTime.of(18, 0))).isEqualTo(LocalTime.of(22, 0));
        assertThat(CondicionesComerciales.horaFinServicio(LocalTime.of(19, 0))).isEqualTo(LocalTime.of(22, 0));
    }
}
