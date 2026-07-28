package com.lacasadelchef.erp.administracion.geografia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MunicipioRequest(

        @NotNull(message = "El departamento es obligatorio")
        Integer idDepartamento,

        @NotBlank(message = "El nombre del municipio es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombreMunicipio
) {
}
