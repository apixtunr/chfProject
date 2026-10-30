package com.lacasadelchef.erp.administracion.rrhh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentoItemRequest(

        @NotNull(message = "El tipo de documento es obligatorio")
        Integer idTipoDocumento,

        @NotBlank(message = "El numero de documento es obligatorio")
        @Size(max = 50, message = "El numero de documento no puede exceder 50 caracteres")
        String numeroDocumento
) {
}
