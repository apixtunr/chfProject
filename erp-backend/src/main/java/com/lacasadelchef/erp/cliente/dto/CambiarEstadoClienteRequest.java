package com.lacasadelchef.erp.cliente.dto;

import jakarta.validation.constraints.NotNull;

/** true = activar, false = inactivar. */
public record CambiarEstadoClienteRequest(
        @NotNull(message = "Indique si el cliente queda activo o inactivo")
        Boolean activo
) {
}
