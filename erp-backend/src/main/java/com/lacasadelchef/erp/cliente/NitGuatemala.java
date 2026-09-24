package com.lacasadelchef.erp.cliente;

import java.util.Locale;
import java.util.Optional;

/**
 * NIT guatemalteco: un cuerpo de digitos y un digito verificador (0-9 o K) que se
 * calcula con modulo 11. Se multiplica cada digito del cuerpo, de derecha a izquierda,
 * por 2, 3, 4...; se suma; el verificador es (11 - suma % 11) % 11, y 10 se escribe K.
 * Ejemplo: 6769359-8.
 *
 * "CF" (consumidor final) es el valor para quien no da NIT, igual que en una factura.
 */
public final class NitGuatemala {

    public static final String CONSUMIDOR_FINAL = "CF";

    private NitGuatemala() {
    }

    /**
     * Devuelve el NIT en formato canonico ("cuerpo-verificador" o "CF"), o vacio si no
     * es valido. Acepta espacios, puntos y guiones en cualquier posicion; vacio o null
     * se toma como consumidor final.
     */
    public static Optional<String> normalizar(String nit) {
        if (nit == null || nit.isBlank()) {
            return Optional.of(CONSUMIDOR_FINAL);
        }
        String limpio = nit.replaceAll("[\\s.\\-/]", "").toUpperCase(Locale.ROOT);
        if (limpio.equals(CONSUMIDOR_FINAL)) {
            return Optional.of(CONSUMIDOR_FINAL);
        }
        if (!limpio.matches("^[0-9]{1,12}[0-9K]$")) {
            return Optional.empty();
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char verificador = limpio.charAt(limpio.length() - 1);
        if (verificador != calcularVerificador(cuerpo)) {
            return Optional.empty();
        }
        return Optional.of(cuerpo + "-" + verificador);
    }

    static char calcularVerificador(String cuerpo) {
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * factor++;
        }
        int resultado = (11 - (suma % 11)) % 11;
        return resultado == 10 ? 'K' : Character.forDigit(resultado, 10);
    }
}
