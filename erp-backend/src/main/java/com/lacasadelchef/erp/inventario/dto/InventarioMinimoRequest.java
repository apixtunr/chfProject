package com.lacasadelchef.erp.inventario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InventarioMinimoRequest(

        @NotNull(message = "La cantidad minima es obligatoria")
        @DecimalMin(value = "0.0", message = "La cantidad minima no puede ser negativa")
        BigDecimal cantidadMinima
) {
}
