package com.lacasadelchef.erp.administracion.geografia.dto;

import com.lacasadelchef.erp.entity.Municipio;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MunicipioResponse(
        Integer idMunicipio,
        Integer idDepartamento,
        String nombreDepartamento,
        String nombreMunicipio,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static MunicipioResponse desde(Municipio municipio) {
        return MunicipioResponse.builder()
                .idMunicipio(municipio.getIdMunicipio())
                .idDepartamento(municipio.getDepartamento().getIdDepartamento())
                .nombreDepartamento(municipio.getDepartamento().getNombreDepartamento())
                .nombreMunicipio(municipio.getNombreMunicipio())
                .fechaCreacion(municipio.getFechaCreacion())
                .fechaModificacion(municipio.getFechaModificacion())
                .build();
    }
}
