package com.lacasadelchef.erp.administracion.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoCostoRequest(

        @NotBlank(message = "El nombre del tipo de costo es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombreTipo,

        @Size(max = 255)
        String descripcion
) {
}
