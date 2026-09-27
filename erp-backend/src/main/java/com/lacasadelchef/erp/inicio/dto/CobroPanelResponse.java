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
        BigDecimal pendiente,
        /**
         * Lo que falta para cubrir el primer 50%, que se paga una semana antes del evento.
         * Solo viene si el evento es en los proximos 7 dias y no se ha cubierto; si no, null.
         */
        BigDecimal anticipoFaltante
) {
}
