package com.lacasadelchef.erp.inicio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CobroPanelResponse(
        Integer idEvento,
        LocalDate fechaEvento,
        String clienteNombre,
        String tipoEventoNombre,
        String estadoNombre,
        BigDecimal total,
        BigDecimal abonado,
        BigDecimal pendiente
) {
}
