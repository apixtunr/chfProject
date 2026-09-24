package com.lacasadelchef.erp.inicio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un producto que los eventos proximos tienen planificado y que el stock actual no
 * alcanza a cubrir. Lo planificado no se reserva: se descuenta hasta que el evento
 * inicia, y si en ese momento no hay suficiente la linea se omite. Esto lo anticipa.
 */
public record InsumoPanelResponse(
        Integer idProducto,
        String nombreProducto,
        String unidadMedida,
        BigDecimal requerido,
        BigDecimal disponible,
        BigDecimal faltante,
        LocalDate primerEvento,
        Long cantidadEventos
) {
}
