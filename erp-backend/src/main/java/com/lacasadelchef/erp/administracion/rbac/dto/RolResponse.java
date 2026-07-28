package com.lacasadelchef.erp.administracion.rbac.dto;

import com.lacasadelchef.erp.entity.Rol;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RolResponse(
        Integer idRol,
        String nombreRol,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static RolResponse desde(Rol rol) {
        return RolResponse.builder()
                .idRol(rol.getIdRol())
                .nombreRol(rol.getNombreRol())
                .fechaCreacion(rol.getFechaCreacion())
                .fechaModificacion(rol.getFechaModificacion())
                .build();
    }
}
