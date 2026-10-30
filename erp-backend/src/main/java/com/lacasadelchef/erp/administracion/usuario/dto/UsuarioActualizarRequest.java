package com.lacasadelchef.erp.administracion.usuario.dto;

import jakarta.validation.constraints.NotNull;

public record UsuarioActualizarRequest(

        @NotNull(message = "El rol es obligatorio")
        Integer idRol,

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado,

        @NotNull(message = "El empleado es obligatorio")
        Integer idEmpleado
) {
}
