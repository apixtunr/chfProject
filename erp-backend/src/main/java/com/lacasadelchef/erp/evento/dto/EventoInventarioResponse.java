package com.lacasadelchef.erp.evento.dto;

import com.lacasadelchef.erp.entity.EventoInventario;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record EventoInventarioResponse(
        Integer idEvento,
        Integer idProducto,
        String nombreProducto,
        BigDecimal cantidad,
        LocalDateTime fechaConsumo
) {

    public static EventoInventarioResponse desde(EventoInventario eventoInventario) {
        return EventoInventarioResponse.builder()
                .idEvento(eventoInventario.getEvento().getIdEvento())
                .idProducto(eventoInventario.getProducto().getIdProducto())
                .nombreProducto(eventoInventario.getProducto().getNombreProducto())
                .cantidad(eventoInventario.getCantidad())
                .fechaConsumo(eventoInventario.getFechaConsumo())
                .build();
    }
}
