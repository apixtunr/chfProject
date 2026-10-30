package com.lacasadelchef.erp.evento.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record EventoResumenResponse(
        long totalEventos,
        long personasAtendidas,
        List<ConteoResponse> porEstado,
        List<ConteoResponse> porTipo
) {
}
