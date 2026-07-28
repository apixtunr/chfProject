package com.lacasadelchef.erp.administracion.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GeneroRequest(

        @NotBlank(message = "El nombre del genero es obligatorio")
        @Size(max = 30, message = "El nombre no puede exceder 30 caracteres")
        String nombreGenero
) {
}
