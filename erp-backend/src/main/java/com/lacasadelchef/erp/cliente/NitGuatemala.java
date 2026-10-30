
package com.lacasadelchef.erp.cliente;

import java.util.Locale;
import java.util.Optional;

/**
 * Valida y normaliza el NIT de los clientes.
 */
public final class NitGuatemala {

    // Valor utilizado cuando el cliente es Consumidor Final.
    public static final String CONSUMIDOR_FINAL = "CF";

    // Evita crear objetos de esta clase.
    private NitGuatemala() {
    }

    /**
     * Limpia el NIT y comprueba que tenga un formato válido.
     *
     * @param nit NIT ingresado por el usuario.
     * @return NIT normalizado o un resultado vacío si no es válido.
     */
    public static Optional<String> normalizar(String nit) {

        // Si no se ingresa un NIT, se utiliza CF.
        if (nit == null || nit.isBlank()) {
            return Optional.of(CONSUMIDOR_FINAL);
        }

        // Elimina separadores y convierte las letras a mayúsculas.
        String limpio = nit.replaceAll("[\\s.\\-/]", "").toUpperCase(Locale.ROOT);

        // Acepta el valor de Consumidor Final.
        if (limpio.equals(CONSUMIDOR_FINAL)) {
            return Optional.of(CONSUMIDOR_FINAL);
        }

        // Comprueba que el NIT tenga el formato esperado.
        if (!limpio.matches("^[0-9]{1,12}[0-9K]$")) {
            return Optional.empty();
        }

        // Separa el número base de su dígito verificador.
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char verificador = limpio.charAt(limpio.length() - 1);

        // Comprueba que el dígito verificador sea correcto.
        if (verificador != calcularVerificador(cuerpo)) {
            return Optional.empty();
        }

        // Devuelve el NIT con un formato uniforme.
        return Optional.of(cuerpo + "-" + verificador);
    }

    // Calcula el dígito verificador del NIT.
    static char calcularVerificador(String cuerpo) {
        int suma = 0;
        int factor = 2;

        // Multiplica cada dígito por un factor creciente.
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * factor++;
        }

        // Obtiene el dígito verificador.
        int resultado = (11 - (suma % 11)) % 11;

        // Si el resultado es 10, utiliza K.
        return resultado == 10 ? 'K' : Character.forDigit(resultado, 10);
    }
}
