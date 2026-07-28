package com.lacasadelchef.erp.cotizacion.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado
) {
}
