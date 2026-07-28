package com.lacasadelchef.erp.administracion.catalogo.dto;

import com.lacasadelchef.erp.entity.TipoEstado;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TipoEstadoResponse(
        Integer idTipoEstado,
        String nombreTipo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static TipoEstadoResponse desde(TipoEstado tipoEstado) {
        return TipoEstadoResponse.builder()
                .idTipoEstado(tipoEstado.getIdTipoEstado())
                .nombreTipo(tipoEstado.getNombreTipo())
                .fechaCreacion(tipoEstado.getFechaCreacion())
                .fechaModificacion(tipoEstado.getFechaModificacion())
                .build();
    }
}
