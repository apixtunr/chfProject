package com.lacasadelchef.erp.administracion.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoEventoRequest(

        @NotBlank(message = "El nombre del tipo de evento es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombreTipo
) {
}
