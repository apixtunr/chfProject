package com.lacasadelchef.erp.bebida.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BebidaRequest(

        @NotBlank(message = "El nombre de la bebida es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombreBebida,

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado
) {
}
