package com.lacasadelchef.erp.pago.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoRequest(

        @NotNull(message = "El evento es obligatorio")
        Integer idEvento,

        @NotNull(message = "El metodo de pago es obligatorio")
        Integer idMetodoPago,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
        BigDecimal monto,

        @Size(max = 100)
        String referenciaTransaccion,

        @Size(max = 255)
        String observaciones,

        LocalDateTime fechaPago,

        /** Si viene con valor, este pago es el reembolso de ese costo extra, no un abono al menu. */
        Integer idCostoEvento
) {
}
