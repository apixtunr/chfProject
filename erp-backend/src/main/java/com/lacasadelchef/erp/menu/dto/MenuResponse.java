package com.lacasadelchef.erp.menu.dto;

import com.lacasadelchef.erp.entity.Menu;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record MenuResponse(
        Integer idMenu,
        String nombreMenu,
        Integer idEstado,
        String estadoNombre,
        /** Suma de los precios de los platos del menu; es el precio que se usa en cotizaciones/eventos. */
        BigDecimal precioTotal,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static MenuResponse desde(Menu menu, BigDecimal precioTotal) {
        return MenuResponse.builder()
                .idMenu(menu.getIdMenu())
                .nombreMenu(menu.getNombreMenu())
                .idEstado(menu.getEstado().getIdEstado())
                .estadoNombre(menu.getEstado().getNombre())
                .precioTotal(precioTotal)
                .fechaCreacion(menu.getFechaCreacion())
                .fechaModificacion(menu.getFechaModificacion())
                .build();
    }
}
