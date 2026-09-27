package com.lacasadelchef.erp.menu.dto;

import com.lacasadelchef.erp.entity.MenuPlato;
import com.lacasadelchef.erp.entity.UnidadVenta;
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
        BigDecimal precioDesde100,
        UnidadVenta unidadVenta,
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
                .precioDesde100(menuPlato.getPrecioDesde100())
                .unidadVenta(menuPlato.getPlato().getUnidadVenta())
                .fechaCreacion(menuPlato.getFechaCreacion())
                .fechaModificacion(menuPlato.getFechaModificacion())
                .build();
    }
}
