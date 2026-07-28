package com.lacasadelchef.erp.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UbicacionRequest(

        /** Nullable: permite registrar salones propios o de terceros, no solo del cliente. */
        Integer idCliente,

        @NotNull(message = "El municipio es obligatorio")
        Integer idMunicipio,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 255, message = "La direccion no puede exceder 255 caracteres")
        String direccion
) {
}
