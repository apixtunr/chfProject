package com.lacasadelchef.erp.administracion.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @NotBlank(message = "El username es obligatorio")
        @Size(max = 50, message = "El username no puede exceder 50 caracteres")
        String username,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Integer idRol,

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado,

        /** Vincula el usuario a un empleado existente; opcional (no todo usuario es empleado). */
        Integer idEmpleado
) {
}
