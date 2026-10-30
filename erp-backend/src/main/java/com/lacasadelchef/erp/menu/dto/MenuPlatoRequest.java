package com.lacasadelchef.erp.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MenuPlatoRequest(

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
        BigDecimal precioUnitario,

        @DecimalMin(value = "0.0", message = "El precio desde 100 personas no puede ser negativo")
        BigDecimal precioDesde100,

        Integer ordenMenu
) {
}
