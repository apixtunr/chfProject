package com.lacasadelchef.erp.rentabilidad.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/** Totales de los eventos finalizados del periodo (mismas cuentas que RentabilidadEventoResponse). */
@Builder
public record RentabilidadResumenResponse(
        long cantidadEventos,
        BigDecimal ingresosAcordados,
        BigDecimal cobrado,
        BigDecimal porCobrar,
        BigDecimal costoPersonal,
        BigDecimal costoInventario,
        BigDecimal costoExtra,
        BigDecimal totalCostos,
        BigDecimal gananciaAcordada,
        BigDecimal gananciaCobrada,
        /** Ganancia acordada total sobre ingresos acordados totales. */
        BigDecimal margen,
        /** Ganancia por tipo de evento, de la mas alta a la mas baja. */
        List<RentabilidadTipoResponse> porTipo
) {

    @Builder
    public record RentabilidadTipoResponse(
            String tipoEventoNombre,
            long cantidadEventos,
            BigDecimal ingresosAcordados,
            BigDecimal gananciaAcordada,
            BigDecimal margen
    ) {
    }
}
