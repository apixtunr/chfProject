package com.lacasadelchef.erp.menu.dto;

import com.lacasadelchef.erp.entity.Menu;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MenuResponse(
        Integer idMenu,
        String nombreMenu,
        Integer idEstado,
        String estadoNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static MenuResponse desde(Menu menu) {
        return MenuResponse.builder()
                .idMenu(menu.getIdMenu())
                .nombreMenu(menu.getNombreMenu())
                .idEstado(menu.getEstado().getIdEstado())
                .estadoNombre(menu.getEstado().getNombre())
                .fechaCreacion(menu.getFechaCreacion())
                .fechaModificacion(menu.getFechaModificacion())
                .build();
    }
}
