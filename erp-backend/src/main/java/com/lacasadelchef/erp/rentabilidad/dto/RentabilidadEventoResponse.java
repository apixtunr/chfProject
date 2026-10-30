package com.lacasadelchef.erp.rentabilidad.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record RentabilidadEventoResponse(
        Integer idEvento,
        LocalDate fechaEvento,
        String tipoEventoNombre,
        Integer idCliente,
        String clienteNombre,

        BigDecimal ingresosAcordados,
        BigDecimal cobrado,
        BigDecimal porCobrar,

        BigDecimal costoPersonal,
        BigDecimal costoInventario,
        BigDecimal costoExtra,
        BigDecimal totalCostos,

        BigDecimal gananciaAcordada,
        BigDecimal gananciaCobrada,
        BigDecimal margen
) {
}
