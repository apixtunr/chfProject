package com.lacasadelchef.erp.pago.dto;

import com.lacasadelchef.erp.entity.MetodoPago;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MetodoPagoResponse(
        Integer idMetodoPago,
        Integer idEstado,
        String estadoNombre,
        String nombreMetodo,
        String descripcion,
        boolean requiereReferencia,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static MetodoPagoResponse desde(MetodoPago metodoPago) {
        return MetodoPagoResponse.builder()
                .idMetodoPago(metodoPago.getIdMetodoPago())
                .idEstado(metodoPago.getEstado().getIdEstado())
                .estadoNombre(metodoPago.getEstado().getNombre())
                .nombreMetodo(metodoPago.getNombreMetodo())
                .descripcion(metodoPago.getDescripcion())
                .requiereReferencia(metodoPago.isRequiereReferencia())
                .fechaCreacion(metodoPago.getFechaCreacion())
                .fechaModificacion(metodoPago.getFechaModificacion())
                .build();
    }
}
