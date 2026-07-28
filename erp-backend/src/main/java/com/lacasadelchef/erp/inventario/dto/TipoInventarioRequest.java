package com.lacasadelchef.erp.inventario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoInventarioRequest(

        @NotBlank(message = "El nombre del tipo de inventario es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombreTipo,

        @Size(max = 255)
        String descripcion
) {
}
