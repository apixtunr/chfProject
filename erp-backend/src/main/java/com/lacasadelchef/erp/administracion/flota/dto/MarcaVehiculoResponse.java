package com.lacasadelchef.erp.administracion.flota.dto;

import com.lacasadelchef.erp.entity.MarcaVehiculo;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MarcaVehiculoResponse(
        Integer idMarcaVehiculo,
        String nombreMarca,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static MarcaVehiculoResponse desde(MarcaVehiculo marca) {
        return MarcaVehiculoResponse.builder()
                .idMarcaVehiculo(marca.getIdMarcaVehiculo())
                .nombreMarca(marca.getNombreMarca())
                .fechaCreacion(marca.getFechaCreacion())
                .fechaModificacion(marca.getFechaModificacion())
                .build();
    }
}
