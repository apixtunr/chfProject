package com.lacasadelchef.erp.cotizacion.dto;

import com.lacasadelchef.erp.entity.ServicioCotizacion;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ServicioCotizacionResponse(
        Integer idServicioCotizacion,
        Integer idCotizacionVersion,
        Integer idTipoServicio,
        String tipoServicioNombre,
        String descripcion,
        BigDecimal monto,
        BigDecimal montoTotalVersion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static ServicioCotizacionResponse desde(ServicioCotizacion servicio) {
        return desde(servicio, servicio.getCotizacionVersion().getMontoTotal());
    }

    public static ServicioCotizacionResponse desde(ServicioCotizacion servicio, BigDecimal montoTotalVersion) {
        return ServicioCotizacionResponse.builder()
                .idServicioCotizacion(servicio.getIdServicioCotizacion())
                .idCotizacionVersion(servicio.getCotizacionVersion().getIdCotizacionVersion())
                .idTipoServicio(servicio.getTipoServicio().getIdTipoServicio())
                .tipoServicioNombre(servicio.getTipoServicio().getNombreTipo())
                .descripcion(servicio.getDescripcion())
                .monto(servicio.getMonto())
                .montoTotalVersion(montoTotalVersion)
                .fechaCreacion(servicio.getFechaCreacion())
                .fechaModificacion(servicio.getFechaModificacion())
                .build();
    }
}
