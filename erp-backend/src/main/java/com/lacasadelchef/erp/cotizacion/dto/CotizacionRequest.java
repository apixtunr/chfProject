package com.lacasadelchef.erp.cotizacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CotizacionRequest(

        @NotNull(message = "El cliente es obligatorio")
        Integer idCliente,

        @NotNull(message = "El tipo de evento es obligatorio")
        Integer idTipoEvento,

        @NotNull(message = "La ubicacion es obligatoria")
        Integer idUbicacion,

        @NotNull(message = "La cantidad de personas es obligatoria")
        @Min(value = 1, message = "La cantidad de personas debe ser mayor a 0")
        Integer cantidadPersonas,

        LocalDate fechaEvento,

        @DecimalMin(value = "0.0", message = "El presupuesto no puede ser negativo")
        BigDecimal presupuestoCliente
) {
}
