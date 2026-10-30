package com.lacasadelchef.erp.evento.dto;

import java.util.List;

public record EventoInventarioConfirmacionMasivaResponse(
        List<EventoInventarioResponse> confirmados,
        List<EventoInventarioFalloResponse> fallidos
) {
}
