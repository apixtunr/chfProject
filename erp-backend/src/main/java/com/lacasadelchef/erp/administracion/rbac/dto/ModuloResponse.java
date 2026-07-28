package com.lacasadelchef.erp.administracion.rbac.dto;

import com.lacasadelchef.erp.entity.Modulo;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ModuloResponse(
        Integer idModulo,
        String nombre,
        Integer orden,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static ModuloResponse desde(Modulo modulo) {
        return ModuloResponse.builder()
                .idModulo(modulo.getIdModulo())
                .nombre(modulo.getNombre())
                .orden(modulo.getOrden())
                .fechaCreacion(modulo.getFechaCreacion())
                .fechaModificacion(modulo.getFechaModificacion())
                .build();
    }
}
