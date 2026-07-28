package com.lacasadelchef.erp.administracion.flota.dto;

import com.lacasadelchef.erp.entity.TipoPlaca;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TipoPlacaResponse(
        Integer idTipoPlaca,
        String nombreTipo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static TipoPlacaResponse desde(TipoPlaca tipoPlaca) {
        return TipoPlacaResponse.builder()
                .idTipoPlaca(tipoPlaca.getIdTipoPlaca())
                .nombreTipo(tipoPlaca.getNombreTipo())
                .fechaCreacion(tipoPlaca.getFechaCreacion())
                .fechaModificacion(tipoPlaca.getFechaModificacion())
                .build();
    }
}
