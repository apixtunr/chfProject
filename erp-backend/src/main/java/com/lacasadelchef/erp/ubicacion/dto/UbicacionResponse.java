package com.lacasadelchef.erp.ubicacion.dto;

import com.lacasadelchef.erp.entity.Ubicacion;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UbicacionResponse(
        Integer idUbicacion,
        Integer idMunicipio,
        String municipioNombre,
        String departamentoNombre,
        String direccion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static UbicacionResponse desde(Ubicacion ubicacion) {
        return UbicacionResponse.builder()
                .idUbicacion(ubicacion.getIdUbicacion())
                .idMunicipio(ubicacion.getMunicipio().getIdMunicipio())
                .municipioNombre(ubicacion.getMunicipio().getNombreMunicipio())
                .departamentoNombre(ubicacion.getMunicipio().getDepartamento().getNombreDepartamento())
                .direccion(ubicacion.getDireccion())
                .fechaCreacion(ubicacion.getFechaCreacion())
                .fechaModificacion(ubicacion.getFechaModificacion())
                .build();
    }
}
