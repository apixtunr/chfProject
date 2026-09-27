package com.lacasadelchef.erp.cotizacion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

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
            + " o 7:00 p.m.). Si necesita que el personal de cocina permanezca más tiempo, la hora extra tiene un"
            + " costo de Q25.00 por cocinero.";

    public static final List<String> OTRAS_CONDICIONES = List.of(
            "Nuestros vasos son solo para bebidas naturales, no para bebidas alcohólicas.",
            "Si sus invitados dañan un vaso o un plato, se cobrará Q15.00 por pieza.",
            "La comida sobrante se empaca y se entrega al anfitrión antes de retirarnos.",
            "Por favor comparta el programa del evento con el personal del buffet para preparar el servicio.",
            "Si necesita dos entradas de buffet, solo se cobra el personal de cocina adicional.",
            "Prueba de menú para 4 personas: se reserva con un mes de anticipación y su costo es el precio del"
                    + " menú elegido.");

    private final int vigenciaDias;

    public CondicionesComerciales(@Value("${app.cotizacion.vigencia-dias:15}") int vigenciaDias) {
        this.vigenciaDias = vigenciaDias;
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
