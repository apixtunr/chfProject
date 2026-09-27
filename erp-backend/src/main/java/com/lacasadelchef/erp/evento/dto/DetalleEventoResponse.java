package com.lacasadelchef.erp.evento.dto;

import com.lacasadelchef.erp.entity.DetalleEvento;
import com.lacasadelchef.erp.bebida.dto.BebidaResumen;
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
        Integer idBebida,
        String nombreBebida,
        /** Bebidas que incluye el plato; si hay y no se eligio ninguna, falta elegirla. */
        List<BebidaResumen> bebidasPlato,
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
                .idBebida(detalle.getBebida() != null ? detalle.getBebida().getIdBebida() : null)
                .nombreBebida(detalle.getBebida() != null ? detalle.getBebida().getNombreBebida() : null)
                .bebidasPlato(BebidaResumen.lista(detalle.getPlato().opcionesBebida()))
                .montoMenuEvento(montoMenuEvento)
                .fechaCreacion(detalle.getFechaCreacion())
                .fechaModificacion(detalle.getFechaModificacion())
                .build();
    }
}
