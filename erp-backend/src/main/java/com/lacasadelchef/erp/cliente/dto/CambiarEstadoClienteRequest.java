package com.lacasadelchef.erp.cliente.dto;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoClienteRequest(
        @NotNull(message = "Indique si el cliente queda activo o inactivo")
        Boolean activo
) {
}
