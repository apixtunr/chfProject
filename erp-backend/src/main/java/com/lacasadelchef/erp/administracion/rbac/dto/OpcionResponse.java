package com.lacasadelchef.erp.administracion.rbac.dto;

import com.lacasadelchef.erp.entity.Opcion;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OpcionResponse(
        Integer idOpcion,
        Integer idMenuVista,
        String menuVistaNombre,
        String moduloNombre,
        String nombreOpcion,
        Integer ordenMenuVista,
        String paginaUrl,
        String accion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static OpcionResponse desde(Opcion opcion) {
        return OpcionResponse.builder()
                .idOpcion(opcion.getIdOpcion())
                .idMenuVista(opcion.getMenuVista().getIdMenuVista())
                .menuVistaNombre(opcion.getMenuVista().getNombre())
                .moduloNombre(opcion.getMenuVista().getModulo().getNombre())
                .nombreOpcion(opcion.getNombreOpcion())
                .ordenMenuVista(opcion.getOrdenMenuVista())
                .paginaUrl(opcion.getPaginaUrl())
                .accion(opcion.getAccion())
                .fechaCreacion(opcion.getFechaCreacion())
                .fechaModificacion(opcion.getFechaModificacion())
                .build();
    }
}
