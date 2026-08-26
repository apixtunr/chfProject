package com.lacasadelchef.erp.evento.dto;

import com.lacasadelchef.erp.entity.DetalleEvento;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record DetalleEventoResponse(
        Integer idDetalleEvento,
        Integer idEvento,
        Integer idMenu,
        String nombreMenu,
        Integer cantidadPlatos,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String observaciones,
        BigDecimal montoMenuEvento,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static DetalleEventoResponse desde(DetalleEvento detalle) {
        return desde(detalle, detalle.getEvento().getMontoMenu());
    }

    public static DetalleEventoResponse desde(DetalleEvento detalle, BigDecimal montoMenuEvento) {
        return DetalleEventoResponse.builder()
                .idDetalleEvento(detalle.getIdDetalleEvento())
                .idEvento(detalle.getEvento().getIdEvento())
                .idMenu(detalle.getMenu().getIdMenu())
                .nombreMenu(detalle.getMenu().getNombreMenu())
                .cantidadPlatos(detalle.getCantidadPlatos())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .observaciones(detalle.getObservaciones())
                .montoMenuEvento(montoMenuEvento)
                .fechaCreacion(detalle.getFechaCreacion())
                .fechaModificacion(detalle.getFechaModificacion())
                .build();
    }
}
