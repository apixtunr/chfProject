package com.lacasadelchef.erp.evento.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventoRequest(

        /** Si viene, el cliente se deriva de la cotizacion (se ignora idCliente). */
        Integer idCotizacionVersion,

        /** Obligatorio solo si idCotizacionVersion viene vacio (evento directo). */
        Integer idCliente,

        /** Obligatorio solo si idCotizacionVersion viene vacio; si no, se toma de la cotizacion. */
        Integer idTipoEvento,

        /** Obligatorio solo si idCotizacionVersion viene vacio; si no, se toma de la cotizacion. */
        Integer idUbicacion,

        /** Obligatorio solo si idCotizacionVersion viene vacio; si no, se toma de la cotizacion. */
        LocalDate fechaEvento,

        LocalTime horaInicio,

        LocalTime horaFin,

        /** Obligatoria solo si idCotizacionVersion viene vacio; si no, se toma de la cotizacion. */
        @Min(value = 1, message = "La cantidad de personas debe ser mayor a 0")
        Integer cantidadPersonas,

        @Size(max = 500)
        String observaciones
) {
}
