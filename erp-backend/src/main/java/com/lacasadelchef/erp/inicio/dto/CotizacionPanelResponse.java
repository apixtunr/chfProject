package com.lacasadelchef.erp.inicio.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record CotizacionPanelResponse(
        Integer idCotizacion,
        Integer idCotizacionVersion,
        Integer numeroVersion,
        String clienteNombre,
        String tipoEventoNombre,
        LocalDate fechaEvento,
        BigDecimal montoTotal,
        LocalDateTime fechaVersion
) {
}
