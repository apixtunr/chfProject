package com.lacasadelchef.erp.evento.dto;

import com.lacasadelchef.erp.entity.DetalleEvento;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DetalleEventoResponse(
        Integer idDetalleEvento,
        Integer idEvento,
        Integer idMenu,
        String nombreMenu,
        Integer idPlato,
        String nombrePlato,
        Integer cantidadPlatos,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String observaciones,
        String bebida,
        /** Bebidas que incluye el plato; si hay y bebida esta vacia, falta elegirla. */
        List<String> bebidasPlato,
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
                .idPlato(detalle.getPlato().getIdPlato())
                .nombrePlato(detalle.getPlato().getNombrePlato())
                .cantidadPlatos(detalle.getCantidadPlatos())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .observaciones(detalle.getObservaciones())
                .bebida(detalle.getBebida())
                .bebidasPlato(detalle.getPlato().opcionesBebida())
                .montoMenuEvento(montoMenuEvento)
                .fechaCreacion(detalle.getFechaCreacion())
                .fechaModificacion(detalle.getFechaModificacion())
                .build();
    }
}
