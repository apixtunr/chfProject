package com.lacasadelchef.erp.administracion.flota.dto;

import com.lacasadelchef.erp.entity.LineaVehiculo;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LineaVehiculoResponse(
        Integer idLineaVehiculo,
        Integer idMarcaVehiculo,
        String nombreMarca,
        String nombreLinea,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static LineaVehiculoResponse desde(LineaVehiculo linea) {
        return LineaVehiculoResponse.builder()
                .idLineaVehiculo(linea.getIdLineaVehiculo())
                .idMarcaVehiculo(linea.getMarcaVehiculo().getIdMarcaVehiculo())
                .nombreMarca(linea.getMarcaVehiculo().getNombreMarca())
                .nombreLinea(linea.getNombreLinea())
                .fechaCreacion(linea.getFechaCreacion())
                .fechaModificacion(linea.getFechaModificacion())
                .build();
    }
}
