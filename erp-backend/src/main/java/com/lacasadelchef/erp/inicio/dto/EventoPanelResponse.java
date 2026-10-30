package com.lacasadelchef.erp.inicio.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
public record EventoPanelResponse(
        Integer idEvento,
        LocalDate fechaEvento,
        LocalTime horaInicio,
        LocalTime horaFin,
        String clienteNombre,
        String tipoEventoNombre,
        String direccionUbicacion,
        Integer cantidadPersonas,
        String estadoNombre,
        boolean requiereMenu,
        boolean tieneMenu,
        boolean tienePersonal,
        boolean tieneVehiculos,
        boolean tieneInventario,
        List<String> faltantes,
        List<String> menus
) {
}
