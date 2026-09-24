package com.lacasadelchef.erp.cliente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NitGuatemalaTest {

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @DisplayName("NIT valido se devuelve en formato canonico")
    @CsvSource({
            "6769359-8, 6769359-8",
            "67693598,  6769359-8",
            "'6769359 8', 6769359-8",
            "576-2,     576-2",
            "cf,        CF",
            "C/F,       CF"
    })
    void validos(String entrada, String esperado) {
        assertThat(NitGuatemala.normalizar(entrada)).contains(esperado);
    }

    @ParameterizedTest
    @DisplayName("Vacio o nulo es consumidor final")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void vacioEsConsumidorFinal(String entrada) {
        assertThat(NitGuatemala.normalizar(entrada)).contains("CF");
    }

    @ParameterizedTest
    @DisplayName("NIT con verificador incorrecto o con letras se rechaza")
    @ValueSource(strings = {"6769359-7", "576-4", "ABC-1", "12-34-5X"})
    void invalidos(String entrada) {
        assertThat(NitGuatemala.normalizar(entrada)).isEmpty();
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Un verificador de 10 se escribe K")
    void verificadorK() {
        // Se busca un cuerpo cuyo verificador sea K y se comprueba que se acepte.
        String cuerpo = java.util.stream.IntStream.range(1, 10_000)
                .mapToObj(String::valueOf)
                .filter(c -> NitGuatemala.calcularVerificador(c) == 'K')
                .findFirst()
                .orElseThrow();
        assertThat(NitGuatemala.normalizar(cuerpo + "k")).contains(cuerpo + "-K");
    }
}
