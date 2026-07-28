package com.lacasadelchef.erp.administracion.catalogo.dto;

import com.lacasadelchef.erp.entity.Genero;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GeneroResponse(
        Integer idGenero,
        String nombreGenero,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static GeneroResponse desde(Genero genero) {
        return GeneroResponse.builder()
                .idGenero(genero.getIdGenero())
                .nombreGenero(genero.getNombreGenero())
                .fechaCreacion(genero.getFechaCreacion())
                .fechaModificacion(genero.getFechaModificacion())
                .build();
    }
}
