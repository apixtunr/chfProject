package com.lacasadelchef.erp.evento.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventoRequest(

        /** Si viene, el cliente se deriva de la cotizacion (se ignora idCliente). */
        Integer idCotizacionVersion,

        /** Obligatorio solo si idCotizacionVersion viene vacio (evento directo). */
        Integer idCliente,

        @NotNull(message = "El tipo de evento es obligatorio")
        Integer idTipoEvento,

        @NotNull(message = "La ubicacion es obligatoria")
        Integer idUbicacion,

        @NotNull(message = "La fecha del evento es obligatoria")
        LocalDate fechaEvento,

        LocalTime horaInicio,

        LocalTime horaFin,

        @Min(value = 1, message = "La cantidad de personas debe ser mayor a 0")
        Integer cantidadPersonas,

        @Size(max = 500)
        String observaciones
) {
}
