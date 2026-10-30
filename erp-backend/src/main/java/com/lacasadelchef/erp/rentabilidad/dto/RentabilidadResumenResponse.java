package com.lacasadelchef.erp.rentabilidad.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

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
        BigDecimal margen,
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
