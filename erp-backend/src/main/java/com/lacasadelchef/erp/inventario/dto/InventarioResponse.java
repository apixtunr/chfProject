package com.lacasadelchef.erp.inventario.dto;

import com.lacasadelchef.erp.entity.Inventario;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record InventarioResponse(
        Integer idInventario,
        Integer idProducto,
        String nombreProducto,
        BigDecimal cantidadTotal,
        BigDecimal cantidadMinima,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static InventarioResponse desde(Inventario inventario) {
        return InventarioResponse.builder()
                .idInventario(inventario.getIdInventario())
                .idProducto(inventario.getProducto().getIdProducto())
                .nombreProducto(inventario.getProducto().getNombreProducto())
                .cantidadTotal(inventario.getCantidadTotal())
                .cantidadMinima(inventario.getCantidadMinima())
                .fechaCreacion(inventario.getFechaCreacion())
                .fechaModificacion(inventario.getFechaModificacion())
                .build();
    }
}
