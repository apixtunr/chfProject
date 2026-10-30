package com.lacasadelchef.erp.inicio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ConsumoPendientePanelResponse(
        Integer idEvento,
        LocalDate fechaEvento,
        String estadoEvento,
        String nombreProducto,
        String unidadMedida,
        BigDecimal cantidad,
        BigDecimal disponible
) {
}
