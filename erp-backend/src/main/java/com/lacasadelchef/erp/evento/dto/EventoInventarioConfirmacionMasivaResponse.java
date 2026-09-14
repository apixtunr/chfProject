package com.lacasadelchef.erp.evento.dto;

import java.util.List;

/** Resultado de confirmar de una vez todo el inventario pendiente de un evento. */
public record EventoInventarioConfirmacionMasivaResponse(
        List<EventoInventarioResponse> confirmados,
        List<EventoInventarioFalloResponse> fallidos
) {
}
