package com.lacasadelchef.erp.rentabilidad;

import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.EventoRepository.RentabilidadFila;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Las cuentas de la rentabilidad: ganancia acordada, ganancia cobrada y lo que falta cobrar. */
@ExtendWith(MockitoExtension.class)
class RentabilidadServiceImplTest {

    @Mock private EventoRepository eventoRepository;
    @InjectMocks private RentabilidadServiceImpl service;

    private static RentabilidadFila fila(int id, String tipo, String precio, String abonado, String reembolsos,
                                        String personal, String inventario, String extra) {
        return new RentabilidadFila() {
            public Integer getIdEvento() { return id; }
            public LocalDate getFechaEvento() { return LocalDate.of(2026, 10, 2); }
            public String getTipoEvento() { return tipo; }
            public Integer getIdCliente() { return 1; }
            public String getClienteNombre() { return "Cliente"; }
            public BigDecimal getPrecio() { return new BigDecimal(precio); }
            public BigDecimal getAbonado() { return new BigDecimal(abonado); }
            public BigDecimal getReembolsos() { return new BigDecimal(reembolsos); }
            public BigDecimal getCostoPersonal() { return new BigDecimal(personal); }
            public BigDecimal getCostoInventario() { return new BigDecimal(inventario); }
            public BigDecimal getCostoExtra() { return new BigDecimal(extra); }
        };
    }

    @Test
    @DisplayName("Un evento de Q2,000 con la mitad cobrada: gana segun lo acordado, pero el dinero que quedo es menor")
    void gananciaAcordadaYCobrada() {
        // Precio 2000, abonado 1000, transporte 100 cobrado al cliente; personal 600, inventario 500.
        var r = RentabilidadServiceImpl.desde(fila(44, "Boda", "2000", "1000", "100", "600", "500", "100"));

        assertThat(r.ingresosAcordados()).isEqualByComparingTo("2100");
        assertThat(r.cobrado()).isEqualByComparingTo("1100");
        assertThat(r.porCobrar()).isEqualByComparingTo("1000");
        assertThat(r.totalCostos()).isEqualByComparingTo("1200");
        assertThat(r.gananciaAcordada()).isEqualByComparingTo("900");
        assertThat(r.gananciaCobrada()).isEqualByComparingTo("-100");
        assertThat(r.margen()).isEqualByComparingTo("42.86");
    }

    @Test
    @DisplayName("Un costo extra que pago el cliente esta de los dos lados: no cambia la ganancia")
    void costoExtraCobradoEsNeutro() {
        var sinExtra = RentabilidadServiceImpl.desde(fila(1, "Boda", "2000", "2000", "0", "600", "500", "0"));
        var conExtra = RentabilidadServiceImpl.desde(fila(1, "Boda", "2000", "2000", "300", "600", "500", "300"));

        assertThat(conExtra.gananciaAcordada()).isEqualByComparingTo(sinExtra.gananciaAcordada());
        assertThat(conExtra.gananciaCobrada()).isEqualByComparingTo(sinExtra.gananciaCobrada());
    }

    @Test
    @DisplayName("Pagado de mas no deja saldo negativo por cobrar")
    void sinSaldoNegativo() {
        var r = RentabilidadServiceImpl.desde(fila(1, "Boda", "2000", "2100", "0", "0", "0", "0"));
        assertThat(r.porCobrar()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("El resumen suma los eventos y agrupa la ganancia por tipo, de mayor a menor")
    void resumenPorTipo() {
        when(eventoRepository.rentabilidadFinalizados(null, null, null, null, null)).thenReturn(List.of(
                fila(1, "Cumpleaños", "1000", "1000", "0", "300", "200", "0"),
                fila(2, "Boda", "4000", "3000", "0", "1200", "800", "0"),
                fila(3, "Boda", "3000", "3000", "0", "900", "600", "0")));

        var r = service.resumen(null, null, null, null);

        assertThat(r.cantidadEventos()).isEqualTo(3);
        assertThat(r.ingresosAcordados()).isEqualByComparingTo("8000");
        assertThat(r.porCobrar()).isEqualByComparingTo("1000");
        assertThat(r.gananciaAcordada()).isEqualByComparingTo("4000");
        assertThat(r.gananciaCobrada()).isEqualByComparingTo("3000");
        assertThat(r.margen()).isEqualByComparingTo("50.00");
        assertThat(r.porTipo()).extracting(t -> t.tipoEventoNombre()).containsExactly("Boda", "Cumpleaños");
        assertThat(r.porTipo().get(0).cantidadEventos()).isEqualTo(2);
        assertThat(r.porTipo().get(0).gananciaAcordada()).isEqualByComparingTo("3500");
    }
}
