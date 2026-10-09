package com.lacasadelchef.erp.pago.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ComprobantePagoRequest(

        @NotBlank(message = "El numero de comprobante es obligatorio")
        @Size(max = 50, message = "El numero de comprobante no puede exceder 50 caracteres")
        String numeroComprobante,

        /** Se ignora: la llena el sistema al guardar el archivo. Se conserva por compatibilidad. */
        @Size(max = 500)
        String archivoUrl,

        /** Uno de ComprobantePagoServiceImpl.TIPOS_COMPROBANTE. */
        @NotBlank(message = "El tipo de comprobante es obligatorio")
        @Size(max = 50)
        String tipoComprobante,

        LocalDate fechaEmision,

        /** Si se omite, se asume valido (true), igual que el default de la base. */
        Boolean esValido
) {
}
