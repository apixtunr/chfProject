package com.lacasadelchef.erp.administracion.catalogo.dto;

import com.lacasadelchef.erp.entity.TipoServicio;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TipoServicioResponse(
        Integer idTipoServicio,
        String nombreTipo,
        String descripcion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static TipoServicioResponse desde(TipoServicio tipoServicio) {
        return TipoServicioResponse.builder()
                .idTipoServicio(tipoServicio.getIdTipoServicio())
                .nombreTipo(tipoServicio.getNombreTipo())
                .descripcion(tipoServicio.getDescripcion())
                .fechaCreacion(tipoServicio.getFechaCreacion())
                .fechaModificacion(tipoServicio.getFechaModificacion())
                .build();
    }
}
