package com.lacasadelchef.erp.security.dto;

import lombok.Builder;

/** Permisos de una opcion de menu para el rol autenticado (consumido por Angular). */
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
