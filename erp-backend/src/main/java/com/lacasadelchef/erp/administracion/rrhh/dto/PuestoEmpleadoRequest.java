package com.lacasadelchef.erp.administracion.rrhh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PuestoEmpleadoRequest(

        @NotBlank(message = "El nombre del puesto es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombreRol
) {
}
