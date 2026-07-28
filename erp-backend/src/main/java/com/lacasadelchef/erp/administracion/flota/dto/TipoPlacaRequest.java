package com.lacasadelchef.erp.administracion.flota.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoPlacaRequest(

        @NotBlank(message = "El nombre del tipo de placa es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
        String nombreTipo
) {
}
