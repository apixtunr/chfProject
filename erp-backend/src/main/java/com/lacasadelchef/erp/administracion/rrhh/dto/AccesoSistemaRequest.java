package com.lacasadelchef.erp.administracion.rrhh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccesoSistemaRequest(


        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Integer idRol
) {
}
