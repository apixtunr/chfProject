package com.lacasadelchef.erp.administracion.catalogo.dto;

import com.lacasadelchef.erp.entity.Estado;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EstadoResponse(
        Integer idEstado,
        Integer idTipoEstado,
        String tipoEstadoNombre,
        String nombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static EstadoResponse desde(Estado estado) {
        return EstadoResponse.builder()
                .idEstado(estado.getIdEstado())
                .idTipoEstado(estado.getTipoEstado().getIdTipoEstado())
                .tipoEstadoNombre(estado.getTipoEstado().getNombreTipo())
                .nombre(estado.getNombre())
                .fechaCreacion(estado.getFechaCreacion())
                .fechaModificacion(estado.getFechaModificacion())
                .build();
    }
}
