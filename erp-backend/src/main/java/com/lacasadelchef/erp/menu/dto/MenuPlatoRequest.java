package com.lacasadelchef.erp.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MenuPlatoRequest(

        /** Precio base (eventos de menos de 100 personas); si el plato va por ciento, el del ciento. */
        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
        BigDecimal precioUnitario,

        /** Opcional: precio para eventos desde 100 personas. Vacio = mismo precio base. */
        @DecimalMin(value = "0.0", message = "El precio desde 100 personas no puede ser negativo")
        BigDecimal precioDesde100,

        Integer ordenMenu
) {
}
