package com.lacasadelchef.erp.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MovimientoInventarioRequest(

        @NotNull(message = "El producto es obligatorio")
        Integer idProducto,

        @NotNull(message = "El tipo de movimiento es obligatorio")
        @Pattern(regexp = "ENTRADA|SALIDA|AJUSTE", message = "El tipo de movimiento debe ser ENTRADA, SALIDA o AJUSTE")
        String tipoMovimiento,

        @NotNull(message = "La cantidad es obligatoria")
        BigDecimal cantidad,

        @Size(max = 255)
        String descripcion,

        Integer idEvento
) {
}
