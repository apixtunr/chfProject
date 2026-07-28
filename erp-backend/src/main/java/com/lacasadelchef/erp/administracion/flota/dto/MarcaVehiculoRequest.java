package com.lacasadelchef.erp.administracion.flota.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarcaVehiculoRequest(

        @NotBlank(message = "El nombre de la marca es obligatorio")
        @Size(max = 60, message = "El nombre no puede exceder 60 caracteres")
        String nombreMarca
) {
}
