package com.lacasadelchef.erp.administracion.catalogo.dto;

import com.lacasadelchef.erp.entity.TipoDocumento;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TipoDocumentoResponse(
        Integer idTipoDocumento,
        String nombreTipo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static TipoDocumentoResponse desde(TipoDocumento tipoDocumento) {
        return TipoDocumentoResponse.builder()
                .idTipoDocumento(tipoDocumento.getIdTipoDocumento())
                .nombreTipo(tipoDocumento.getNombreTipo())
                .fechaCreacion(tipoDocumento.getFechaCreacion())
                .fechaModificacion(tipoDocumento.getFechaModificacion())
                .build();
    }
}
