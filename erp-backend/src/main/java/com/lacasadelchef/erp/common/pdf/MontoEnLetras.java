package com.lacasadelchef.erp.common.pdf;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Un monto en quetzales escrito en letras, como se acostumbra en recibos y cheques:
 * 1500.00 -> "Un mil quinientos quetzales con 00/100".
 *
 * Alcanza hasta 999 999 999.99, de sobra para lo que se cobra por un evento.
 */
public final class MontoEnLetras {

    private static final String[] UNIDADES = {
            "", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve",
            "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho",
            "diecinueve", "veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco",
            "veintiséis", "veintisiete", "veintiocho", "veintinueve"};
    private static final String[] DECENAS = {
            "", "", "", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
    private static final String[] CENTENAS = {
            "", "ciento", "doscientos", "trescientos", "cuatrocientos", "quinientos", "seiscientos",
            "setecientos", "ochocientos", "novecientos"};

    private MontoEnLetras() {
    }

    public static String quetzales(BigDecimal monto) {
        BigDecimal valor = monto.setScale(2, RoundingMode.HALF_UP);
        if (valor.signum() < 0 || valor.compareTo(new BigDecimal("999999999.99")) > 0) {
            throw new IllegalArgumentException("Monto fuera de rango: " + monto);
        }
        long enteros = valor.longValue();
        int centavos = valor.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        // "uno" se apocopa delante de un sustantivo: "veintiún quetzales", "un quetzal".
        String letras = enteros == 0 ? "cero" : apocopar(numero(enteros));
        // "un millón de quetzales", pero "un millón doscientos mil quetzales".
        String moneda = enteros == 1 ? "quetzal"
                : enteros >= 1_000_000 && enteros % 1_000_000 == 0 ? "de quetzales" : "quetzales";
        String texto = "%s %s con %02d/100".formatted(letras, moneda, centavos);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private static String numero(long n) {
        if (n >= 1_000_000) {
            long millones = n / 1_000_000;
            String parte = millones == 1 ? "un millón" : apocopar(numero(millones)) + " millones";
            return unir(parte, n % 1_000_000);
        }
        if (n >= 1000) {
            long miles = n / 1000;
            // "un mil" y no "mil" a secas: es la forma usual en documentos de pago en Guatemala.
            String parte = apocopar(numero(miles)) + " mil";
            return unir(parte, n % 1000);
        }
        return centenas((int) n);
    }

    private static String centenas(int n) {
        if (n == 100) {
            return "cien";
        }
        String centena = CENTENAS[n / 100];
        int resto = n % 100;
        String decenas = resto < 30 ? UNIDADES[resto]
                : DECENAS[resto / 10] + (resto % 10 == 0 ? "" : " y " + UNIDADES[resto % 10]);
        return (centena + " " + decenas).trim();
    }

    private static String unir(String parte, long resto) {
        return resto == 0 ? parte : parte + " " + numero(resto);
    }

    /** Delante de un sustantivo ("mil", "millones", "quetzales"): "veintiún mil", "treinta y un quetzales". */
    private static String apocopar(String letras) {
        if (letras.endsWith("veintiuno")) {
            return letras.substring(0, letras.length() - 3) + "ún";
        }
        return letras.endsWith("uno") ? letras.substring(0, letras.length() - 1) : letras;
    }
}
