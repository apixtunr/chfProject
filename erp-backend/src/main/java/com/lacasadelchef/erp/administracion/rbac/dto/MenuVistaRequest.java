package com.lacasadelchef.erp.administracion.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuVistaRequest(

        @NotNull(message = "El modulo es obligatorio")
        Integer idModulo,

        @NotBlank(message = "El nombre de la vista es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        Integer orden
) {
}
