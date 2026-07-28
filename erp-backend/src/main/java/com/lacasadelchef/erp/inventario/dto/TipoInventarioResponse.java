package com.lacasadelchef.erp.inventario.dto;

import com.lacasadelchef.erp.entity.TipoInventario;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TipoInventarioResponse(
        Integer idTipoInventario,
        String nombreTipo,
        String descripcion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static TipoInventarioResponse desde(TipoInventario tipoInventario) {
        return TipoInventarioResponse.builder()
                .idTipoInventario(tipoInventario.getIdTipoInventario())
                .nombreTipo(tipoInventario.getNombreTipo())
                .descripcion(tipoInventario.getDescripcion())
                .fechaCreacion(tipoInventario.getFechaCreacion())
                .fechaModificacion(tipoInventario.getFechaModificacion())
                .build();
    }
}
