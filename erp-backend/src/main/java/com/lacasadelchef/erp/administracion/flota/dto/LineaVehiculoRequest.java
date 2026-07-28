package com.lacasadelchef.erp.administracion.flota.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LineaVehiculoRequest(

        @NotNull(message = "La marca es obligatoria")
        Integer idMarcaVehiculo,

        @NotBlank(message = "El nombre de la linea es obligatorio")
        @Size(max = 60, message = "El nombre no puede exceder 60 caracteres")
        String nombreLinea
) {
}
