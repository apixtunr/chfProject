package com.lacasadelchef.erp.evento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CancelarEventoRequest(

        @NotBlank(message = "Indique el motivo de la cancelación")
        @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
        String motivo,

        /** RETENIDO, DEVUELTO o DEVUELTO_PARCIAL; obligatorio si el cliente ya pago algo. */
        String acuerdoAnticipo,

        /** Solo para DEVUELTO_PARCIAL: cuanto se le devuelve. */
        @DecimalMin(value = "0.0", message = "El monto devuelto no puede ser negativo")
        BigDecimal montoDevuelto
) {
}
