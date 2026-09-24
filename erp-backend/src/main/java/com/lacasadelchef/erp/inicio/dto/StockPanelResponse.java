package com.lacasadelchef.erp.inicio.dto;

import java.math.BigDecimal;

public record StockPanelResponse(
        Integer idProducto,
        String nombreProducto,
        String unidadMedida,
        BigDecimal cantidadActual,
        BigDecimal cantidadMinima
) {
}
