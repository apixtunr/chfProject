package com.lacasadelchef.erp.administracion.rrhh.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmpleadoRequest(

        @NotNull(message = "El puesto es obligatorio")
        Integer idPuestoEmpleado,

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado,

        Integer idGenero,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
        String apellido,

        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 150)
        String correo,

        @Pattern(regexp = "^[0-9+\\- ]{8,20}$", message = "El telefono no tiene un formato valido")
        String telefono,

        LocalDate fechaContratacion
) {
}
