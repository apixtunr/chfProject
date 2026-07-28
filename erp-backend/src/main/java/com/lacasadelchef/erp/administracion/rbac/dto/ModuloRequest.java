package com.lacasadelchef.erp.administracion.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModuloRequest(

        @NotBlank(message = "El nombre del modulo es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombre,

        Integer orden
) {
}
