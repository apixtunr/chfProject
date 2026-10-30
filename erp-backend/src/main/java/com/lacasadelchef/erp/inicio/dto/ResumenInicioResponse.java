package com.lacasadelchef.erp.inicio.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ResumenInicioResponse(
        Long eventosSemana,
        Long eventosPorPreparar,
        Long cotizacionesEnviadas,
        Long productosBajoStock,
        BigDecimal saldoPendiente,
        Long eventosConSaldo
) {
}
