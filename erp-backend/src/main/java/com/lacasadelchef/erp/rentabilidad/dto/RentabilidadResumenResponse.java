package com.lacasadelchef.erp.rentabilidad.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RentabilidadResumenResponse(
        long cantidadEventos,
        BigDecimal totalIngresos,
        BigDecimal totalCostos,
        BigDecimal gananciaTotal,
        BigDecimal margenPromedio
) {
}
