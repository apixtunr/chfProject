package com.lacasadelchef.erp.pago.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado
) {
}
