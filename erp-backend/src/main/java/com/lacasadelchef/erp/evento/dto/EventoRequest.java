package com.lacasadelchef.erp.evento.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventoRequest(

        Integer idCotizacionVersion,

        Integer idCliente,

        Integer idTipoEvento,

        Integer idUbicacion,

        LocalDate fechaEvento,

        LocalTime horaInicio,

        LocalTime horaFin,

        @Min(value = 1, message = "La cantidad de personas debe ser mayor a 0")
        Integer cantidadPersonas,

        @Size(max = 500)
        String observaciones
) {
}
