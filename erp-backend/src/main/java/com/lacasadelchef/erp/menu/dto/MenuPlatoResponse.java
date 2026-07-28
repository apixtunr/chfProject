package com.lacasadelchef.erp.menu.dto;

import com.lacasadelchef.erp.entity.MenuPlato;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MenuPlatoResponse(
        Integer idMenu,
        Integer idPlato,
        String nombrePlato,
        Integer ordenMenu,
        BigDecimal precioUnitario,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static MenuPlatoResponse desde(MenuPlato menuPlato) {
        return MenuPlatoResponse.builder()
                .idMenu(menuPlato.getMenu().getIdMenu())
                .idPlato(menuPlato.getPlato().getIdPlato())
                .nombrePlato(menuPlato.getPlato().getNombrePlato())
                .ordenMenu(menuPlato.getOrdenMenu())
                .precioUnitario(menuPlato.getPrecioUnitario())
                .fechaCreacion(menuPlato.getFechaCreacion())
                .fechaModificacion(menuPlato.getFechaModificacion())
                .build();
    }
}
