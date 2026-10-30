package com.lacasadelchef.erp.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum UnidadVenta {

    PERSONA(1),
    CIENTO(100),
    UNIDAD(1);

    private final int unidadesPorPrecio;

    UnidadVenta(int unidadesPorPrecio) {
        this.unidadesPorPrecio = unidadesPorPrecio;
    }

    public BigDecimal precioPorUnidad(BigDecimal precioCatalogo) {
        return precioCatalogo.divide(BigDecimal.valueOf(unidadesPorPrecio), 2, RoundingMode.HALF_UP);
    }
}
