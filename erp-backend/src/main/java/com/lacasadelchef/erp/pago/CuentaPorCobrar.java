package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.cotizacion.CondicionesComerciales;
import com.lacasadelchef.erp.entity.VPagoEvento;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Lo que le toca pagar a un evento con saldo y para cuando, segun la forma de pago de la
 * empresa: el 50 % una semana antes del evento y el resto al finalizar.
 *
 * @param vence fecha en que vence lo que toca pagar (el anticipo o el saldo final)
 * @param dias  dias que faltan para que venza; negativo si ya esta atrasado
 */
public record CuentaPorCobrar(VPagoEvento evento, BigDecimal faltaAnticipo, Situacion situacion, LocalDate vence,
                              long dias) {

    private static final String ESTADO_FINALIZADO = "FINALIZADO";

    /**
     * ANTICIPO    todavia no cubre el 50 %, que vence una semana antes del evento;
     * SALDO_FINAL ya cubrio el anticipo y le falta el resto, que se paga al finalizar;
     * VENCIDO     el evento ya finalizo y quedo saldo sin pagar.
     */
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
        // Un evento finalizado con saldo ya esta vencido, aunque haya terminado hoy.
        if (situacion == Situacion.VENCIDO && dias == 0) {
            dias = -1;
        }
        return new CuentaPorCobrar(v, faltaAnticipo, situacion, vence, dias);
    }

    /** "En 5 días", "Hoy" o "Atrasado 3 días". */
    public String textoVencimiento() {
        if (dias == 0) {
            return "Hoy";
        }
        long n = Math.abs(dias);
        String texto = n + (n == 1 ? " día" : " días");
        return dias > 0 ? "En " + texto : "Atrasado " + texto;
    }
}
