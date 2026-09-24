package com.lacasadelchef.erp.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos de entrada para crear o actualizar un cliente. */
public record ClienteRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
        String nombre,

        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 150)
        String correo,

        @Pattern(regexp = "^[0-9+\\- ]{8,20}$", message = "El telefono no tiene un formato valido")
        String telefono,

        /** Opcional: vacio se guarda como "CF". Se valida el digito verificador. */
        @Size(max = 20)
        String nit,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 255, message = "La direccion no puede exceder 255 caracteres")
        String direccion,

        @NotNull(message = "El municipio es obligatorio")
        Integer idMunicipio
) {
}
