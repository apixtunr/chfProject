package com.lacasadelchef.erp.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Como se lee el precio de un plato del catalogo. La cantidad que se cotiza es cuantas
 * personas eligen el plato o, en las boquitas, cuantas piezas; lo que cambia es a cuantas
 * de ellas corresponde el precio.
 */
public enum UnidadVenta {

    /** Precio por persona (platos fuertes, menu de ninos, refacciones, desayunos). */
    PERSONA(1),
    /** Precio por 100 unidades (boquitas: "Q800.00 el ciento"). */
    CIENTO(100),
    /** Precio por pieza ("Bola de queso Q75.00"). */
    UNIDAD(1);

    private final int unidadesPorPrecio;

    UnidadVenta(int unidadesPorPrecio) {
        this.unidadesPorPrecio = unidadesPorPrecio;
    }

    /** Precio por persona o por pieza: el ciento de Q800.00 da Q8.00 por unidad. */
    public BigDecimal precioPorUnidad(BigDecimal precioCatalogo) {
        return precioCatalogo.divide(BigDecimal.valueOf(unidadesPorPrecio), 2, RoundingMode.HALF_UP);
    }
}
