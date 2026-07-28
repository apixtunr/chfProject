package com.lacasadelchef.erp.administracion.rbac.dto;

public record RolOpcionRequest(
        boolean alta,
        boolean baja,
        boolean modificacion,
        boolean imprimir,
        boolean exportar
) {
}
