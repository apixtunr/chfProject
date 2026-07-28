package com.lacasadelchef.erp.pago.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MetodoPagoRequest(

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado,

        @NotBlank(message = "El nombre del metodo de pago es obligatorio")
        @Size(max = 60, message = "El nombre no puede exceder 60 caracteres")
        String nombreMetodo,

        @Size(max = 255)
        String descripcion,

        boolean requiereReferencia
) {
}
