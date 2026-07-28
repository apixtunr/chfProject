package com.lacasadelchef.erp.inventario.dto;

import com.lacasadelchef.erp.entity.Producto;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProductoResponse(
        Integer idProducto,
        Integer idCategoria,
        String nombreCategoria,
        String nombreProducto,
        String unidadMedida,
        BigDecimal precioUnitario,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static ProductoResponse desde(Producto producto) {
        return ProductoResponse.builder()
                .idProducto(producto.getIdProducto())
                .idCategoria(producto.getCategoria().getIdCategoria())
                .nombreCategoria(producto.getCategoria().getNombreCategoria())
                .nombreProducto(producto.getNombreProducto())
                .unidadMedida(producto.getUnidadMedida())
                .precioUnitario(producto.getPrecioUnitario())
                .fechaCreacion(producto.getFechaCreacion())
                .fechaModificacion(producto.getFechaModificacion())
                .build();
    }
}
