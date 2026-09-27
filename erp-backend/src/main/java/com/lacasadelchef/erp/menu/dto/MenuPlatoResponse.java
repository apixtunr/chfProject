package com.lacasadelchef.erp.menu.dto;

import com.lacasadelchef.erp.entity.MenuPlato;
import com.lacasadelchef.erp.entity.UnidadVenta;
import com.lacasadelchef.erp.bebida.dto.BebidaResumen;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MenuPlatoResponse(
        Integer idMenu,
        Integer idPlato,
        String nombrePlato,
        Integer ordenMenu,
        BigDecimal precioUnitario,
        BigDecimal precioDesde100,
        UnidadVenta unidadVenta,
        /** Bebidas que incluye el plato, para elegir una al cotizarlo. */
        List<BebidaResumen> bebidas,
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
                .bebidas(BebidaResumen.lista(menuPlato.getPlato().opcionesBebida()))
                .fechaCreacion(menuPlato.getFechaCreacion())
                .fechaModificacion(menuPlato.getFechaModificacion())
                .build();
    }
}
