package com.lacasadelchef.erp.administracion.flota.dto;

import com.lacasadelchef.erp.entity.Vehiculo;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record VehiculoResponse(
        Integer idVehiculo,
        Integer idLineaVehiculo,
        String nombreLinea,
        String nombreMarca,
        Integer idTipoPlaca,
        String tipoPlacaNombre,
        Integer idEstado,
        String estadoNombre,
        String placa,
        Integer anioVehiculo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static VehiculoResponse desde(Vehiculo vehiculo) {
        return VehiculoResponse.builder()
                .idVehiculo(vehiculo.getIdVehiculo())
                .idLineaVehiculo(vehiculo.getLineaVehiculo().getIdLineaVehiculo())
                .nombreLinea(vehiculo.getLineaVehiculo().getNombreLinea())
                .nombreMarca(vehiculo.getLineaVehiculo().getMarcaVehiculo().getNombreMarca())
                .idTipoPlaca(vehiculo.getTipoPlaca().getIdTipoPlaca())
                .tipoPlacaNombre(vehiculo.getTipoPlaca().getNombreTipo())
                .idEstado(vehiculo.getEstado().getIdEstado())
                .estadoNombre(vehiculo.getEstado().getNombre())
                .placa(vehiculo.getPlaca())
                .anioVehiculo(vehiculo.getAnioVehiculo())
                .fechaCreacion(vehiculo.getFechaCreacion())
                .fechaModificacion(vehiculo.getFechaModificacion())
                .build();
    }
}
