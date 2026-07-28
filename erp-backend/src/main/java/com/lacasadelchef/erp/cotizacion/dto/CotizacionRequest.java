package com.lacasadelchef.erp.cotizacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CotizacionRequest(

        @NotNull(message = "El cliente es obligatorio")
        Integer idCliente,

        LocalDate fechaEvento,

        @DecimalMin(value = "0.0", message = "El presupuesto no puede ser negativo")
        BigDecimal presupuestoCliente
) {
}
