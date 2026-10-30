package com.lacasadelchef.erp.inicio.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

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
