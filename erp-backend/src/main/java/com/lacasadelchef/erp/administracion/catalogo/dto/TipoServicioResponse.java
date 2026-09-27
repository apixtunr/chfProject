package com.lacasadelchef.erp.administracion.catalogo.dto;

import com.lacasadelchef.erp.entity.TipoServicio;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TipoServicioResponse(
        Integer idTipoServicio,
        String nombreTipo,
        String descripcion,
        BigDecimal precioUnitario,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static TipoServicioResponse desde(TipoServicio tipoServicio) {
        return TipoServicioResponse.builder()
                .idTipoServicio(tipoServicio.getIdTipoServicio())
                .nombreTipo(tipoServicio.getNombreTipo())
                .descripcion(tipoServicio.getDescripcion())
                .precioUnitario(tipoServicio.getPrecioUnitario())
                .fechaCreacion(tipoServicio.getFechaCreacion())
                .fechaModificacion(tipoServicio.getFechaModificacion())
                .build();
    }
}
