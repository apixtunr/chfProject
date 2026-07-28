package com.lacasadelchef.erp.administracion.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoDocumentoRequest(

        @NotBlank(message = "El nombre del tipo de documento es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
        String nombreTipo
) {
}
