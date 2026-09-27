package com.lacasadelchef.erp.cotizacion.dto;

import com.lacasadelchef.erp.entity.DetalleCotizacion;
import com.lacasadelchef.erp.bebida.dto.BebidaResumen;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
        Integer idBebida,
        String nombreBebida,
        /** Bebidas que incluye el plato; si hay y no se eligio ninguna, falta elegirla. */
        List<BebidaResumen> bebidasPlato,
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
                .idBebida(detalle.getBebida() != null ? detalle.getBebida().getIdBebida() : null)
                .nombreBebida(detalle.getBebida() != null ? detalle.getBebida().getNombreBebida() : null)
                .bebidasPlato(BebidaResumen.lista(detalle.getPlato().opcionesBebida()))
                .montoTotalVersion(montoTotalVersion)
                .fechaCreacion(detalle.getFechaCreacion())
                .fechaModificacion(detalle.getFechaModificacion())
                .build();
    }
}
