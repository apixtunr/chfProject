package com.lacasadelchef.erp.inicio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InsumoPanelResponse(
        Integer idProducto,
        String nombreProducto,
        String unidadMedida,
        BigDecimal requerido,
        BigDecimal disponible,
        BigDecimal faltante,
        LocalDate primerEvento,
        Long cantidadEventos
) {
}
