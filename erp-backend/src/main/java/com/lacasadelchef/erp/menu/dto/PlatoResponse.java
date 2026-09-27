package com.lacasadelchef.erp.menu.dto;

import com.lacasadelchef.erp.entity.Plato;
import com.lacasadelchef.erp.entity.UnidadVenta;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PlatoResponse(
        Integer idPlato,
        String nombrePlato,
        Integer idEstado,
        String estadoNombre,
        UnidadVenta unidadVenta,
        List<String> bebidas,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static PlatoResponse desde(Plato plato) {
        return PlatoResponse.builder()
                .idPlato(plato.getIdPlato())
                .nombrePlato(plato.getNombrePlato())
                .idEstado(plato.getEstado().getIdEstado())
                .estadoNombre(plato.getEstado().getNombre())
                .unidadVenta(plato.getUnidadVenta())
                .bebidas(plato.opcionesBebida())
                .fechaCreacion(plato.getFechaCreacion())
                .fechaModificacion(plato.getFechaModificacion())
                .build();
    }
}
