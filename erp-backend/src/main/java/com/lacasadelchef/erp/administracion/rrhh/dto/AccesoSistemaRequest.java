package com.lacasadelchef.erp.administracion.rrhh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos del acceso al sistema que se le crea a un empleado al darlo de alta.
 *
 * Va dentro de {@link EmpleadoRequest} y es opcional: la mayoria de los empleados no
 * entra al sistema. Cuando viene, el empleado y su usuario se graban en la misma
 * transaccion, de modo que si algo falla (por ejemplo, el username ya existe) no queda
 * el empleado creado sin su usuario.
 *
 * El estado del usuario no se pide: nace ACTIVO. Para desactivarlo despues esta la
 * pantalla de Usuarios.
 */
public record AccesoSistemaRequest(

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(max = 50, message = "El nombre de usuario no puede exceder 50 caracteres")
        String username,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        String password,

        /** Rol del sistema. No tiene relacion con el puesto del empleado. */
        @NotNull(message = "El rol es obligatorio")
        Integer idRol
) {
}
