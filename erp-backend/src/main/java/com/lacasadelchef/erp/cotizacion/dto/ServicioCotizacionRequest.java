package com.lacasadelchef.erp.cotizacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicioCotizacionRequest(

        @NotNull(message = "El tipo de servicio es obligatorio")
        Integer idTipoServicio,

        @Size(max = 255)
        String descripcion,

        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        Integer cantidad,

        @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
        BigDecimal monto
) {
}
