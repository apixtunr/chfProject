package com.lacasadelchef.erp.administracion.flota.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehiculoRequest(

        @NotNull(message = "La linea de vehiculo es obligatoria")
        Integer idLineaVehiculo,

        @NotNull(message = "El tipo de placa es obligatorio")
        Integer idTipoPlaca,

        @NotNull(message = "El estado es obligatorio")
        Integer idEstado,

        @NotBlank(message = "La placa es obligatoria")
        @Size(max = 20, message = "La placa no puede exceder 20 caracteres")
        String placa,

        Integer anioVehiculo
) {
}
