package com.lacasadelchef.erp.menu.dto;

import com.lacasadelchef.erp.entity.Plato;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PlatoResponse(
        Integer idPlato,
        String nombrePlato,
        Integer idEstado,
        String estadoNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static PlatoResponse desde(Plato plato) {
        return PlatoResponse.builder()
                .idPlato(plato.getIdPlato())
                .nombrePlato(plato.getNombrePlato())
                .idEstado(plato.getEstado().getIdEstado())
                .estadoNombre(plato.getEstado().getNombre())
                .fechaCreacion(plato.getFechaCreacion())
                .fechaModificacion(plato.getFechaModificacion())
                .build();
    }
}
