package com.lacasadelchef.erp.inventario.dto;

import com.lacasadelchef.erp.entity.MovimientoInventario;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MovimientoInventarioResponse(
        Long idMovimiento,
        Integer idProducto,
        String nombreProducto,
        Integer idEvento,
        Integer idUsuario,
        String usernameUsuario,
        String tipoMovimiento,
        BigDecimal cantidad,
        String descripcion,
        LocalDateTime fechaMovimiento,
        BigDecimal cantidadTotalActual
) {

    public static MovimientoInventarioResponse desde(MovimientoInventario movimiento, BigDecimal cantidadTotalActual) {
        return MovimientoInventarioResponse.builder()
                .idMovimiento(movimiento.getIdMovimiento())
                .idProducto(movimiento.getProducto().getIdProducto())
                .nombreProducto(movimiento.getProducto().getNombreProducto())
                .idEvento(movimiento.getEvento() != null ? movimiento.getEvento().getIdEvento() : null)
                .idUsuario(movimiento.getUsuario() != null ? movimiento.getUsuario().getIdUsuario() : null)
                .usernameUsuario(movimiento.getUsuario() != null ? movimiento.getUsuario().getUsername() : null)
                .tipoMovimiento(movimiento.getTipoMovimiento())
                .cantidad(movimiento.getCantidad())
                .descripcion(movimiento.getDescripcion())
                .fechaMovimiento(movimiento.getFechaMovimiento())
                .cantidadTotalActual(cantidadTotalActual)
                .build();
    }
}
