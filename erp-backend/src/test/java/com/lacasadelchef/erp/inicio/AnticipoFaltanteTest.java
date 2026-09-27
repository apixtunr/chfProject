package com.lacasadelchef.erp.inicio;

import com.lacasadelchef.erp.entity.VPagoEvento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** El primer 50% se paga una semana antes del evento (forma de pago del menu). */
class AnticipoFaltanteTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 1);

    private static VPagoEvento evento(LocalDate fecha, String total, String abonado) {
        VPagoEvento v = new VPagoEvento();
        ReflectionTestUtils.setField(v, "fechaEvento", fecha);
        ReflectionTestUtils.setField(v, "total", new BigDecimal(total));
        ReflectionTestUtils.setField(v, "abonado", new BigDecimal(abonado));
        return v;
    }

    @Test
    @DisplayName("Evento en 5 dias con Q1,000 de Q5,000 abonados: faltan Q1,500 del 50%")
    void faltaParteDelAnticipo() {
        assertThat(InicioServiceImpl.anticipoFaltante(evento(HOY.plusDays(5), "5000.00", "1000.00"), HOY))
                .isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("Con el 50% cubierto no hay aviso")
    void anticipoCubierto() {
        assertThat(InicioServiceImpl.anticipoFaltante(evento(HOY.plusDays(5), "5000.00", "2500.00"), HOY)).isNull();
    }

    @Test
    @DisplayName("Faltando mas de una semana todavia no se avisa")
    void todaviaNoToca() {
        assertThat(InicioServiceImpl.anticipoFaltante(evento(HOY.plusDays(8), "5000.00", "0"), HOY)).isNull();
    }

    @Test
    @DisplayName("Justo a 7 dias y el mismo dia del evento si se avisa")
    void bordesDeLaSemana() {
        assertThat(InicioServiceImpl.anticipoFaltante(evento(HOY.plusDays(7), "5000.00", "0"), HOY))
                .isEqualByComparingTo("2500.00");
        assertThat(InicioServiceImpl.anticipoFaltante(evento(HOY, "5000.00", "0"), HOY))
                .isEqualByComparingTo("2500.00");
    }

    @Test
    @DisplayName("Un evento que ya paso no se avisa como anticipo (ya se debe todo)")
    void eventoPasado() {
        assertThat(InicioServiceImpl.anticipoFaltante(evento(HOY.minusDays(1), "5000.00", "0"), HOY)).isNull();
    }
}
