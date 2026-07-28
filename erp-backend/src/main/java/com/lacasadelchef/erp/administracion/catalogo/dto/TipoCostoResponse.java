package com.lacasadelchef.erp.administracion.catalogo.dto;

import com.lacasadelchef.erp.entity.TipoCosto;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TipoCostoResponse(
        Integer idTipoCosto,
        String nombreTipo,
        String descripcion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static TipoCostoResponse desde(TipoCosto tipoCosto) {
        return TipoCostoResponse.builder()
                .idTipoCosto(tipoCosto.getIdTipoCosto())
                .nombreTipo(tipoCosto.getNombreTipo())
                .descripcion(tipoCosto.getDescripcion())
                .fechaCreacion(tipoCosto.getFechaCreacion())
                .fechaModificacion(tipoCosto.getFechaModificacion())
                .build();
    }
}
