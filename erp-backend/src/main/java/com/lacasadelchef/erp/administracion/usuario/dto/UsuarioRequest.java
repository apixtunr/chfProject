package com.lacasadelchef.erp.administracion.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Alta de usuario para un empleado. No trae nombre de usuario: lo genera el sistema con
 * la politica de la empresa (ver {@link com.lacasadelchef.erp.administracion.usuario.PoliticaUsuario}).
 */
public record UsuarioRequest(


        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Integer idRol,

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado,

        /**
         * Empleado al que pertenece el usuario. Obligatorio: todo usuario es una persona
         * del negocio. Al reves no: la mayoria de los empleados no tiene usuario.
         */
        @NotNull(message = "El empleado es obligatorio")
        Integer idEmpleado
) {
}
