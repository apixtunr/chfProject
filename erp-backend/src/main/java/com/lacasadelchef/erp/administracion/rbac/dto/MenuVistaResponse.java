package com.lacasadelchef.erp.administracion.rbac.dto;

import com.lacasadelchef.erp.entity.MenuVista;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MenuVistaResponse(
        Integer idMenuVista,
        Integer idModulo,
        String moduloNombre,
        String nombre,
        Integer orden,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static MenuVistaResponse desde(MenuVista menuVista) {
        return MenuVistaResponse.builder()
                .idMenuVista(menuVista.getIdMenuVista())
                .idModulo(menuVista.getModulo().getIdModulo())
                .moduloNombre(menuVista.getModulo().getNombre())
                .nombre(menuVista.getNombre())
                .orden(menuVista.getOrden())
                .fechaCreacion(menuVista.getFechaCreacion())
                .fechaModificacion(menuVista.getFechaModificacion())
                .build();
    }
}
