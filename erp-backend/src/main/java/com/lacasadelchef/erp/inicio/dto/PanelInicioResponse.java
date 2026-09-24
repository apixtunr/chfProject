package com.lacasadelchef.erp.inicio.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

/**
 * Todo lo que muestra la pantalla de inicio. Cada seccion es null cuando el rol del
 * usuario no tiene acceso a la pantalla de origen (por ejemplo, Cocina no recibe cobros):
 * null significa "no aplica", una lista vacia significa "aplica, pero no hay nada".
 */
@Builder
public record PanelInicioResponse(
        LocalDate fechaReferencia,
        String rol,
        ResumenInicioResponse resumen,
        List<EventoPanelResponse> porPreparar,
        List<EventoPanelResponse> agenda,
        List<CotizacionPanelResponse> cotizacionesEnviadas,
        List<CotizacionPanelResponse> cotizacionesAceptadasSinEvento,
        List<StockPanelResponse> bajoStock,
        List<InsumoPanelResponse> insumosFaltantes,
        List<CobroPanelResponse> cobrosPendientes,
        ActividadPanelResponse actividad
) {
}
