package com.lacasadelchef.erp.evento.dto;

import com.lacasadelchef.erp.entity.Evento;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
public record EventoResponse(
        Integer idEvento,
        Integer idCotizacionVersion,
        Integer idCliente,
        String clienteNombre,
        Integer idTipoEvento,
        String tipoEventoNombre,
        Integer idUbicacion,
        String direccionUbicacion,
        Integer idEstado,
        String estadoNombre,
        LocalDate fechaEvento,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer cantidadPersonas,
        String observaciones,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static EventoResponse desde(Evento evento) {
        boolean tieneCotizacion = evento.getCotizacionVersion() != null;
        return EventoResponse.builder()
                .idEvento(evento.getIdEvento())
                .idCotizacionVersion(tieneCotizacion ? evento.getCotizacionVersion().getIdCotizacionVersion() : null)
                .idCliente(clienteDe(evento).getIdCliente())
                .clienteNombre(clienteDe(evento).getNombre())
                .idTipoEvento(evento.getTipoEvento().getIdTipoEvento())
                .tipoEventoNombre(evento.getTipoEvento().getNombreTipo())
                .idUbicacion(evento.getUbicacion().getIdUbicacion())
                .direccionUbicacion(evento.getUbicacion().getDireccion())
                .idEstado(evento.getEstado().getIdEstado())
                .estadoNombre(evento.getEstado().getNombre())
                .fechaEvento(evento.getFechaEvento())
                .horaInicio(evento.getHoraInicio())
                .horaFin(evento.getHoraFin())
                .cantidadPersonas(evento.getCantidadPersonas())
                .observaciones(evento.getObservaciones())
                .fechaCreacion(evento.getFechaCreacion())
                .fechaModificacion(evento.getFechaModificacion())
                .build();
    }

    /** El cliente sale de la cotizacion si la hay, o del campo directo si no. */
    private static com.lacasadelchef.erp.entity.Cliente clienteDe(Evento evento) {
        return evento.getCotizacionVersion() != null
                ? evento.getCotizacionVersion().getCotizacion().getCliente()
                : evento.getCliente();
    }
}
