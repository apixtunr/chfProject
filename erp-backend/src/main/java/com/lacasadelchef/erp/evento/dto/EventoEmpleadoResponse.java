package com.lacasadelchef.erp.evento.dto;

import com.lacasadelchef.erp.entity.EventoEmpleado;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
public record EventoEmpleadoResponse(
        Integer idEvento,
        Integer idEmpleado,
        String nombreEmpleado,
        Integer idEstado,
        String estadoNombre,
        BigDecimal salarioEvento,
        LocalDateTime fechaAsignacion,
        LocalTime horaInicio,
        LocalTime horaFin
) {

    public static EventoEmpleadoResponse desde(EventoEmpleado eventoEmpleado) {
        return EventoEmpleadoResponse.builder()
                .idEvento(eventoEmpleado.getEvento().getIdEvento())
                .idEmpleado(eventoEmpleado.getEmpleado().getIdEmpleado())
                .nombreEmpleado(eventoEmpleado.getEmpleado().getNombre() + " " + eventoEmpleado.getEmpleado().getApellido())
                .idEstado(eventoEmpleado.getEstado().getIdEstado())
                .estadoNombre(eventoEmpleado.getEstado().getNombre())
                .salarioEvento(eventoEmpleado.getSalarioEvento())
                .fechaAsignacion(eventoEmpleado.getFechaAsignacion())
                .horaInicio(eventoEmpleado.getHoraInicio())
                .horaFin(eventoEmpleado.getHoraFin())
                .build();
    }
}
