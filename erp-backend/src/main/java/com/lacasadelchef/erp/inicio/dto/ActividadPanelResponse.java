package com.lacasadelchef.erp.inicio.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Resumen de la bitacora del dia, para quien tiene acceso a ella. */
public record ActividadPanelResponse(
        long ingresosHoy,
        long ingresosFallidosHoy,
        long cambiosHoy,
        List<MovimientoReciente> recientes
) {

    public record MovimientoReciente(
            String usuario,
            String tabla,
            String registroId,
            String operacion,
            LocalDateTime fecha
    ) {
    }
}
