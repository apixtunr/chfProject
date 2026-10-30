package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.cotizacion.CondicionesComerciales;
import com.lacasadelchef.erp.entity.VPagoEvento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record CuentaPorCobrar(VPagoEvento evento, BigDecimal faltaAnticipo, Situacion situacion, LocalDate vence,
                              long dias) {

    private static final String ESTADO_FINALIZADO = "FINALIZADO";

    public enum Situacion {
        ANTICIPO("Anticipo pendiente"),
        SALDO_FINAL("Saldo al finalizar"),
        VENCIDO("Saldo vencido");

        private final String etiqueta;

        Situacion(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String etiqueta() {
            return etiqueta;
        }
    }

    public static CuentaPorCobrar de(VPagoEvento v, LocalDate hoy) {
        BigDecimal total = v.getTotal() == null ? BigDecimal.ZERO : v.getTotal();
        BigDecimal abonado = v.getAbonado() == null ? BigDecimal.ZERO : v.getAbonado();
        BigDecimal anticipo = total.multiply(CondicionesComerciales.PORCION_ANTICIPO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal faltaAnticipo = anticipo.subtract(abonado).max(BigDecimal.ZERO);

        Situacion situacion;
        LocalDate vence;
        if (ESTADO_FINALIZADO.equalsIgnoreCase(v.getEstadoNombre())) {
            situacion = Situacion.VENCIDO;
            vence = v.getFechaEvento();
        } else if (faltaAnticipo.signum() > 0) {
            situacion = Situacion.ANTICIPO;
            vence = v.getFechaEvento().minusDays(CondicionesComerciales.DIAS_ANTICIPACION);
        } else {
            situacion = Situacion.SALDO_FINAL;
            vence = v.getFechaEvento();
        }
        long dias = ChronoUnit.DAYS.between(hoy, vence);
        if (situacion == Situacion.VENCIDO && dias == 0) {
            dias = -1;
        }
        return new CuentaPorCobrar(v, faltaAnticipo, situacion, vence, dias);
    }

    public String textoVencimiento() {
        if (dias == 0) {
            return "Hoy";
        }
        long n = Math.abs(dias);
        String texto = n + (n == 1 ? " día" : " días");
        return dias > 0 ? "En " + texto : "Atrasado " + texto;
    }
}
