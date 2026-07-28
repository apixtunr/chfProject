package com.lacasadelchef.erp.administracion.geografia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartamentoRequest(

        @NotBlank(message = "El nombre del departamento es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombreDepartamento
) {
}
