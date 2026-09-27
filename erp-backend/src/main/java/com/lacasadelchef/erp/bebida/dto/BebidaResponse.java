package com.lacasadelchef.erp.bebida.dto;

import com.lacasadelchef.erp.entity.Bebida;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BebidaResponse(
        Integer idBebida,
        String nombreBebida,
        Integer idEstado,
        String estadoNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static BebidaResponse desde(Bebida bebida) {
        return BebidaResponse.builder()
                .idBebida(bebida.getIdBebida())
                .nombreBebida(bebida.getNombreBebida())
                .idEstado(bebida.getEstado().getIdEstado())
                .estadoNombre(bebida.getEstado().getNombre())
                .fechaCreacion(bebida.getFechaCreacion())
                .fechaModificacion(bebida.getFechaModificacion())
                .build();
    }
}
