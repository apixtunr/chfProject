package com.lacasadelchef.erp.administracion.catalogo.dto;

import com.lacasadelchef.erp.entity.TipoEvento;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TipoEventoResponse(
        Integer idTipoEvento,
        String nombreTipo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static TipoEventoResponse desde(TipoEvento tipoEvento) {
        return TipoEventoResponse.builder()
                .idTipoEvento(tipoEvento.getIdTipoEvento())
                .nombreTipo(tipoEvento.getNombreTipo())
                .fechaCreacion(tipoEvento.getFechaCreacion())
                .fechaModificacion(tipoEvento.getFechaModificacion())
                .build();
    }
}
