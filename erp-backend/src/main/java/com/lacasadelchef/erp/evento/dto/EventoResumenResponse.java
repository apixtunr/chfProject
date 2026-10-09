package com.lacasadelchef.erp.evento.dto;

import lombok.Builder;

import java.util.List;

/** Conteos para el dashboard de eventos (tarjetas y graficas). */
@Builder
public record EventoResumenResponse(
        long totalEventos,
        /** Suma de invitados de los eventos finalizados del periodo. */
        long personasAtendidas,
        List<ConteoResponse> porEstado,
        List<ConteoResponse> porTipo
) {
}
