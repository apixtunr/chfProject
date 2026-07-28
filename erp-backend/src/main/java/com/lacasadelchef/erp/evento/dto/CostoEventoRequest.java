package com.lacasadelchef.erp.evento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CostoEventoRequest(

        @NotNull(message = "El tipo de costo es obligatorio")
        Integer idTipoCosto,

        @Size(max = 255)
        String descripcion,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.0", message = "El monto no puede ser negativo")
        BigDecimal monto,

        LocalDate fechaCosto
) {
}
