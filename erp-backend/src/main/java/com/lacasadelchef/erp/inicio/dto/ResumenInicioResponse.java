package com.lacasadelchef.erp.inicio.dto;

import lombok.Builder;

import java.math.BigDecimal;

/** Cifras de la fila de indicadores. Igual que en el panel: null = el rol no lo ve. */
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
