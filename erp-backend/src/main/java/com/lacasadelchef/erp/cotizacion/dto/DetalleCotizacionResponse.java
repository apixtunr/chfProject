package com.lacasadelchef.erp.cotizacion.dto;

import com.lacasadelchef.erp.entity.DetalleCotizacion;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record DetalleCotizacionResponse(
        Integer idDetalleCotizacion,
        Integer idCotizacionVersion,
        Integer idMenu,
        String nombreMenu,
        Integer idPlato,
        String nombrePlato,
        Integer cantidadPlatos,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String observaciones,
        BigDecimal montoTotalVersion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static DetalleCotizacionResponse desde(DetalleCotizacion detalle) {
        return desde(detalle, detalle.getCotizacionVersion().getMontoTotal());
    }

    public static DetalleCotizacionResponse desde(DetalleCotizacion detalle, BigDecimal montoTotalVersion) {
        return DetalleCotizacionResponse.builder()
                .idDetalleCotizacion(detalle.getIdDetalleCotizacion())
                .idCotizacionVersion(detalle.getCotizacionVersion().getIdCotizacionVersion())
                .idMenu(detalle.getMenu().getIdMenu())
                .nombreMenu(detalle.getMenu().getNombreMenu())
                .idPlato(detalle.getPlato().getIdPlato())
                .nombrePlato(detalle.getPlato().getNombrePlato())
                .cantidadPlatos(detalle.getCantidadPlatos())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .observaciones(detalle.getObservaciones())
                .montoTotalVersion(montoTotalVersion)
                .fechaCreacion(detalle.getFechaCreacion())
                .fechaModificacion(detalle.getFechaModificacion())
                .build();
    }
}
