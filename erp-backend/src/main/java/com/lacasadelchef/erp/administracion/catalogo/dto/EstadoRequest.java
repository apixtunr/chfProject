package com.lacasadelchef.erp.administracion.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EstadoRequest(

        @NotNull(message = "El tipo de estado es obligatorio")
        Integer idTipoEstado,

        @NotBlank(message = "El nombre del estado es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
        String nombre
) {
}
