package com.lacasadelchef.erp.cotizacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicioCotizacionRequest(

        @NotNull(message = "El tipo de servicio es obligatorio")
        Integer idTipoServicio,

        @Size(max = 255)
        String descripcion,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
        BigDecimal monto
) {
}
