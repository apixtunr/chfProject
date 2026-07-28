package com.lacasadelchef.erp.administracion.usuario.dto;

import jakarta.validation.constraints.NotNull;

/** No incluye username ni password: se cambian por endpoints dedicados. */
public record UsuarioActualizarRequest(

        @NotNull(message = "El rol es obligatorio")
        Integer idRol,

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado,

        Integer idEmpleado
) {
}
