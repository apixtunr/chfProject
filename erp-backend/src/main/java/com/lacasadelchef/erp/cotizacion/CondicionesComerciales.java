package com.lacasadelchef.erp.cotizacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Condiciones que la empresa ofrece en toda cotizacion, tomadas de su menu de banquetes
 * 2026. Estan en un solo lugar para que el PDF (y lo que se construya despues, como el
 * vencimiento de las cotizaciones) no repita ni contradiga lo que la empresa promete.
 *
 * La vigencia se puede cambiar sin tocar el codigo con app.cotizacion.vigencia-dias.
 */
@Component
public class CondicionesComerciales {

    public static final String FORMA_DE_PAGO = "50% una semana antes del evento y 50% al finalizar el evento.";

    public static final List<String> EL_SERVICIO_INCLUYE = List.of(
            "Montaje de buffet (con cubremantel color café; puede alquilar cubremanteles extra para combinarlo"
                    + " con el mobiliario de su evento).",
            "Transporte, debidamente higiénico.",
            "Cocineros para atender el buffet (no atienden las mesas ni recogen la cristalería).",
            "Plato de loza, vaso de vidrio, tenedor y cuchillo de metal, y servilletas de papel.");

    public static final String HORARIO = "El servicio de alimentos es de 4 horas a partir de la hora solicitada y"
            + " concluye como máximo a las 9:00 p.m. (hasta las 10:00 p.m. para servicios que inician a las 6:00"
            + " o 7:00 p.m.). Los desayunos inician entre las 7:00 y las 10:00 a.m. y concluyen como máximo a las"
            + " 11:00 a.m. Si necesita que el personal de cocina permanezca más tiempo, la hora extra tiene un"
            + " costo de Q25.00 por cocinero.";

    public static final List<String> OTRAS_CONDICIONES = List.of(
            "Nuestros vasos son solo para bebidas naturales, no para bebidas alcohólicas.",
            "Si sus invitados dañan un vaso o un plato, se cobrará Q15.00 por pieza.",
            "La comida sobrante se empaca y se entrega al anfitrión antes de retirarnos.",
            "Por favor comparta el programa del evento con el personal del buffet para preparar el servicio.",
            "Si necesita dos entradas de buffet, solo se cobra el personal de cocina adicional.",
            "Prueba de menú para 4 personas: se reserva con un mes de anticipación y su costo es el precio del"
                    + " menú elegido.");

    /**
     * Anticipacion minima para reservar: el primer 50% se paga una semana antes del
     * evento, asi que no se agenda (ni se cotiza) un evento para dentro de menos de 7 dias.
     */
    public static final int DIAS_ANTICIPACION = 7;

    /** Porcion del total que se paga una semana antes del evento. */
    public static final BigDecimal PORCION_ANTICIPO = new BigDecimal("0.50");

    /** El servicio dura 4 horas desde la hora solicitada. */
    public static final int HORAS_DE_SERVICIO = 4;

    /**
     * Turnos del menu: el servicio empieza en punto entre las 7:00 y las 19:00. De 7:00 a
     * 10:00 son desayunos; de 11:00 en adelante, almuerzos y cenas.
     */
    public static final List<LocalTime> HORAS_DE_INICIO = IntStream.rangeClosed(7, 19)
            .mapToObj(hora -> LocalTime.of(hora, 0))
            .toList();

    /** Un desayuno (inicio de 7:00 a 10:00) termina a mas tardar a las 11:00. */
    private static final LocalTime CIERRE_DESAYUNO = LocalTime.of(11, 0);
    private static final LocalTime CIERRE = LocalTime.of(21, 0);
    /** Excepcion del menu: si empieza a las 18 o 19, se atiende hasta las 22:00. */
    private static final LocalTime CIERRE_NOCTURNO = LocalTime.of(22, 0);
    private static final LocalTime INICIO_NOCTURNO = LocalTime.of(18, 0);

    private final int vigenciaDias;

    public CondicionesComerciales(@Value("${app.cotizacion.vigencia-dias:15}") int vigenciaDias) {
        this.vigenciaDias = vigenciaDias;
    }

    /** Primer dia en que se puede agendar un evento si se reserva hoy. */
    public static LocalDate fechaMinimaEvento(LocalDate hoy) {
        return hoy.plusDays(DIAS_ANTICIPACION);
    }

    public static boolean esHoraDeInicioPermitida(LocalTime inicio) {
        return HORAS_DE_INICIO.contains(inicio);
    }

    /**
     * Fin del servicio: 4 horas despues del inicio, sin pasar de las 21:00 (22:00 si
     * empieza a las 18 o 19). Un desayuno termina a mas tardar a las 11:00: el de las 7:00
     * dura sus 4 horas, el de las 9:00 hasta las 11:00.
     */
    public static LocalTime horaFinServicio(LocalTime inicio) {
        LocalTime cierre = inicio.isBefore(CIERRE_DESAYUNO) ? CIERRE_DESAYUNO
                : inicio.isBefore(INICIO_NOCTURNO) ? CIERRE : CIERRE_NOCTURNO;
        LocalTime fin = inicio.plusHours(HORAS_DE_SERVICIO);
        return fin.isAfter(cierre) ? cierre : fin;
    }

    public int vigenciaDias() {
        return vigenciaDias;
    }

    /** Lo que dice la cotizacion sobre su plazo: el precio se respeta mientras esta vigente. */
    public String textoVigencia() {
        return "%d días. Los precios de esta cotización se respetan durante ese plazo; pasado ese tiempo pueden"
                .formatted(vigenciaDias) + " actualizarse.";
    }
}
