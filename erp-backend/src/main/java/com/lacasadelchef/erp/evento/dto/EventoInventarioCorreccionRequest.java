package com.lacasadelchef.erp.evento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EventoInventarioCorreccionRequest(

        @NotNull(message = "La cantidad correcta es obligatoria")
        @DecimalMin(value = "0.0", message = "La cantidad no puede ser negativa")
        BigDecimal cantidadCorrecta
) {
}
