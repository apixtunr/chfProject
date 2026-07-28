package com.lacasadelchef.erp.evento.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado
) {
}
