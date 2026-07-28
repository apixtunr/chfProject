package com.lacasadelchef.erp.cotizacion.dto;

import com.lacasadelchef.erp.entity.CotizacionVersion;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CotizacionVersionResponse(
        Integer idCotizacionVersion,
        Integer idCotizacion,
        Integer idEstado,
        String estadoNombre,
        Integer numeroVersion,
        BigDecimal montoTotal,
        LocalDateTime fechaVersion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static CotizacionVersionResponse desde(CotizacionVersion version) {
        return CotizacionVersionResponse.builder()
                .idCotizacionVersion(version.getIdCotizacionVersion())
                .idCotizacion(version.getCotizacion().getIdCotizacion())
                .idEstado(version.getEstado().getIdEstado())
                .estadoNombre(version.getEstado().getNombre())
                .numeroVersion(version.getNumeroVersion())
                .montoTotal(version.getMontoTotal())
                .fechaVersion(version.getFechaVersion())
                .fechaCreacion(version.getFechaCreacion())
                .fechaModificacion(version.getFechaModificacion())
                .build();
    }
}
