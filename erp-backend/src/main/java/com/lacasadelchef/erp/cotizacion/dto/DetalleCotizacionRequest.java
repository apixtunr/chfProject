package com.lacasadelchef.erp.cotizacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DetalleCotizacionRequest(

        @NotNull(message = "El menu es obligatorio")
        Integer idMenu,

        @NotNull(message = "La cantidad de platos es obligatoria")
        @Min(value = 1, message = "La cantidad de platos debe ser mayor a 0")
        Integer cantidadPlatos,

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
        BigDecimal precioUnitario,

        @Size(max = 255)
        String observaciones
) {
}
