package com.lacasadelchef.erp.security.dto;

import lombok.Builder;

@Builder
public record PermisoResponse(
        String modulo,
        String menuVista,
        String opcion,
        String paginaUrl,
        boolean alta,
        boolean baja,
        boolean modificacion,
        boolean imprimir,
        boolean exportar
) {
}
