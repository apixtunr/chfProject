package com.lacasadelchef.erp.evento.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DetalleEventoRequest(

        @NotNull(message = "El menu es obligatorio")
        Integer idMenu,

        @NotNull(message = "El plato es obligatorio")
        Integer idPlato,

        @NotNull(message = "La cantidad de platos es obligatoria")
        @Min(value = 1, message = "La cantidad de platos debe ser mayor a 0")
        Integer cantidadPlatos,

        @Size(max = 255)
        String observaciones
) {
}
