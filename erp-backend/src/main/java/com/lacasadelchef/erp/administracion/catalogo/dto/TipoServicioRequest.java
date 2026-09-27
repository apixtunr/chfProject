package com.lacasadelchef.erp.administracion.catalogo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TipoServicioRequest(

        @NotBlank(message = "El nombre del tipo de servicio es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombreTipo,

        @Size(max = 255)
        String descripcion,

        /** Opcional: precio por unidad (ej. por cocinero y hora). Vacio = monto libre al cotizar. */
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        BigDecimal precioUnitario,

        /** Opcional: false = ya no se ofrece al cotizar. Vacio = activo. */
        Boolean activo
) {
}
