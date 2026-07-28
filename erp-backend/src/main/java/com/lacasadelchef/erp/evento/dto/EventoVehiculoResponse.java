package com.lacasadelchef.erp.evento.dto;

import com.lacasadelchef.erp.entity.EventoVehiculo;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventoVehiculoResponse(
        Integer idEvento,
        Integer idVehiculo,
        String placaVehiculo,
        Integer idEmpleadoConductor,
        String nombreConductor,
        LocalDateTime fechaAsignacion
) {

    public static EventoVehiculoResponse desde(EventoVehiculo eventoVehiculo) {
        return EventoVehiculoResponse.builder()
                .idEvento(eventoVehiculo.getEvento().getIdEvento())
                .idVehiculo(eventoVehiculo.getVehiculo().getIdVehiculo())
                .placaVehiculo(eventoVehiculo.getVehiculo().getPlaca())
                .idEmpleadoConductor(eventoVehiculo.getEmpleado() != null ? eventoVehiculo.getEmpleado().getIdEmpleado() : null)
                .nombreConductor(eventoVehiculo.getEmpleado() != null
                        ? eventoVehiculo.getEmpleado().getNombre() + " " + eventoVehiculo.getEmpleado().getApellido()
                        : null)
                .fechaAsignacion(eventoVehiculo.getFechaAsignacion())
                .build();
    }
}
