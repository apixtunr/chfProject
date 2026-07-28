package com.lacasadelchef.erp.administracion.rbac.dto;

import com.lacasadelchef.erp.entity.RolOpcion;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RolOpcionResponse(
        Integer idRol,
        String nombreRol,
        Integer idOpcion,
        String nombreOpcion,
        String paginaUrl,
        boolean alta,
        boolean baja,
        boolean modificacion,
        boolean imprimir,
        boolean exportar,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static RolOpcionResponse desde(RolOpcion rolOpcion) {
        return RolOpcionResponse.builder()
                .idRol(rolOpcion.getRol().getIdRol())
                .nombreRol(rolOpcion.getRol().getNombreRol())
                .idOpcion(rolOpcion.getOpcion().getIdOpcion())
                .nombreOpcion(rolOpcion.getOpcion().getNombreOpcion())
                .paginaUrl(rolOpcion.getOpcion().getPaginaUrl())
                .alta(rolOpcion.isAlta())
                .baja(rolOpcion.isBaja())
                .modificacion(rolOpcion.isModificacion())
                .imprimir(rolOpcion.isImprimir())
                .exportar(rolOpcion.isExportar())
                .fechaCreacion(rolOpcion.getFechaCreacion())
                .fechaModificacion(rolOpcion.getFechaModificacion())
                .build();
    }
}
