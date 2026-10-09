package com.lacasadelchef.erp.common.reporte;

import com.lacasadelchef.erp.entity.VPagoEvento;
import com.lacasadelchef.erp.pago.CuentaPorCobrar;
import com.lacasadelchef.erp.pago.CuentaPorCobrar.Situacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Los reportes en tabla: agrupacion por periodo, subtotales y filas que no suman. */
class ReporteTablaBuilderTest {

    private record Pago(String recibo, LocalDate fecha, String monto, boolean anulado) {
    }

    private static final List<Pago> PAGOS = List.of(
            new Pago("REC-1", LocalDate.of(2026, 10, 9), "100", false),   // jueves
            new Pago("REC-2", LocalDate.of(2026, 9, 30), "50", false),    // mes anterior
            new Pago("REC-3", LocalDate.of(2026, 10, 5), "70", true),     // lunes, anulado
            new Pago("REC-4", LocalDate.of(2026, 10, 11), "30", false));  // domingo

    private static ReporteTabla reporte(String agrupar) {
        return ReporteTablaBuilder.de("Recibos", PAGOS)
                .texto("recibo", "Recibo", 1f, Pago::recibo)
                .monto("monto", "Monto", 1f, true, p -> new BigDecimal(p.monto()))
                .fechaDeAgrupacion(Pago::fecha)
                .excluirDeTotales(Pago::anulado)
                .construir(agrupar);
    }

    @Test
    @DisplayName("Sin agrupar: un solo grupo; el anulado aparece pero no suma")
    void sinAgrupar() {
        ReporteTabla r = reporte(null);

        assertThat(r.grupos()).hasSize(1);
        assertThat(r.grupos().getFirst().etiqueta()).isNull();
        assertThat(r.grupos().getFirst().filas()).hasSize(4);
        assertThat(r.totales().get("monto")).isEqualByComparingTo("180");
        assertThat(r.cantidad()).isEqualTo(3);
        assertThat(r.excluidas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Por mes: en orden cronologico, con su subtotal")
    void porMes() {
        ReporteTabla r = reporte("MES");

        assertThat(r.grupos()).extracting(ReporteTabla.Grupo::etiqueta).containsExactly("Septiembre 2026", "Octubre 2026");
        assertThat(r.grupos().get(1).subtotales().get("monto")).isEqualByComparingTo("130");
    }

    @Test
    @DisplayName("Por semana: de lunes a domingo")
    void porSemana() {
        ReporteTabla r = reporte("SEMANA");

        assertThat(r.grupos()).extracting(ReporteTabla.Grupo::etiqueta).containsExactly(
                "Semana del 28/09/2026 al 04/10/2026", "Semana del 05/10/2026 al 11/10/2026");
        assertThat(r.grupos().get(1).filas()).hasSize(3);
    }

    @Test
    @DisplayName("Cuentas por cobrar: anticipo pendiente, saldo final y vencido")
    void situacionDeCobro() {
        LocalDate hoy = LocalDate.of(2026, 10, 9);
        CuentaPorCobrar anticipo = CuentaPorCobrar.de(evento("PLANIFICADO", hoy.plusDays(20), "4000", "500"), hoy);
        CuentaPorCobrar atrasado = CuentaPorCobrar.de(evento("PLANIFICADO", hoy.plusDays(3), "4000", "0"), hoy);
        CuentaPorCobrar saldo = CuentaPorCobrar.de(evento("PLANIFICADO", hoy.plusDays(10), "4000", "2000"), hoy);
        CuentaPorCobrar vencido = CuentaPorCobrar.de(evento("FINALIZADO", hoy, "4000", "2000"), hoy);

        assertThat(anticipo.situacion()).isEqualTo(Situacion.ANTICIPO);
        assertThat(anticipo.faltaAnticipo()).isEqualByComparingTo("1500");
        assertThat(anticipo.textoVencimiento()).isEqualTo("En 13 días");
        assertThat(atrasado.textoVencimiento()).isEqualTo("Atrasado 4 días");
        assertThat(saldo.situacion()).isEqualTo(Situacion.SALDO_FINAL);
        assertThat(vencido.situacion()).isEqualTo(Situacion.VENCIDO);
        assertThat(vencido.dias()).isNegative();
    }

    private static VPagoEvento evento(String estado, LocalDate fecha, String total, String abonado) {
        VPagoEvento v = mock(VPagoEvento.class);
        when(v.getEstadoNombre()).thenReturn(estado);
        when(v.getFechaEvento()).thenReturn(fecha);
        when(v.getTotal()).thenReturn(new BigDecimal(total));
        when(v.getAbonado()).thenReturn(new BigDecimal(abonado));
        return v;
    }
}
