package com.lacasadelchef.erp.administracion.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OpcionRequest(

        @NotNull(message = "La vista de menu es obligatoria")
        Integer idMenuVista,

        @NotBlank(message = "El nombre de la opcion es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String nombreOpcion,

        Integer ordenMenuVista,

        /** Path base del controlador (ej. '/api/clientes'); usado por PermisoService para autorizar. */
        @Size(max = 255)
        String paginaUrl,

        @Size(max = 80)
        String accion
) {
}
