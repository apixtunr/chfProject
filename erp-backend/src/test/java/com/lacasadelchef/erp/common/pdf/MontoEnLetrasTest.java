package com.lacasadelchef.erp.common.pdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MontoEnLetrasTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @DisplayName("Montos en letras como en un recibo")
    @CsvSource(delimiter = '|', value = {
            "0.50       | Cero quetzales con 50/100",
            "1          | Un quetzal con 00/100",
            "21         | Veintiún quetzales con 00/100",
            "31.05      | Treinta y un quetzales con 05/100",
            "100        | Cien quetzales con 00/100",
            "115        | Ciento quince quetzales con 00/100",
            "1000       | Un mil quetzales con 00/100",
            "1500       | Un mil quinientos quetzales con 00/100",
            "2000       | Dos mil quetzales con 00/100",
            "21000      | Veintiún mil quetzales con 00/100",
            "101000     | Ciento un mil quetzales con 00/100",
            "999999.99  | Novecientos noventa y nueve mil novecientos noventa y nueve quetzales con 99/100",
            "1000000    | Un millón de quetzales con 00/100",
            "3000000    | Tres millones de quetzales con 00/100",
            "2350475.10 | Dos millones trescientos cincuenta mil cuatrocientos setenta y cinco quetzales con 10/100",
    })
    void enLetras(BigDecimal monto, String esperado) {
        assertThat(MontoEnLetras.quetzales(monto)).isEqualTo(esperado);
    }
}
