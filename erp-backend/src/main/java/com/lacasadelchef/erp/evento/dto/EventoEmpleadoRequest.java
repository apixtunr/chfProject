package com.lacasadelchef.erp.evento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalTime;

public record EventoEmpleadoRequest(

        @NotNull(message = "El salario del evento es obligatorio")
        @DecimalMin(value = "0.0", message = "El salario no puede ser negativo")
        BigDecimal salarioEvento,

        LocalTime horaInicio,

        LocalTime horaFin,

        Integer idEstado
) {
}
