package com.lacasadelchef.erp.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuRequest(

        @NotBlank(message = "El nombre del menu es obligatorio")
        @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
        String nombreMenu,

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado
) {
}
