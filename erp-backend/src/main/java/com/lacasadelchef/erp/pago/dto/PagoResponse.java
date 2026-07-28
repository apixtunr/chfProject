package com.lacasadelchef.erp.pago.dto;

import com.lacasadelchef.erp.entity.Pago;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PagoResponse(
        Integer idPago,
        Integer idEvento,
        Integer idUsuario,
        String usernameUsuario,
        Integer idMetodoPago,
        String nombreMetodoPago,
        Integer idEstado,
        String estadoNombre,
        BigDecimal monto,
        String referenciaTransaccion,
        String observaciones,
        LocalDateTime fechaPago,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static PagoResponse desde(Pago pago) {
        return PagoResponse.builder()
                .idPago(pago.getIdPago())
                .idEvento(pago.getEvento().getIdEvento())
                .idUsuario(pago.getUsuario().getIdUsuario())
                .usernameUsuario(pago.getUsuario().getUsername())
                .idMetodoPago(pago.getMetodoPago().getIdMetodoPago())
                .nombreMetodoPago(pago.getMetodoPago().getNombreMetodo())
                .idEstado(pago.getEstado().getIdEstado())
                .estadoNombre(pago.getEstado().getNombre())
                .monto(pago.getMonto())
                .referenciaTransaccion(pago.getReferenciaTransaccion())
                .observaciones(pago.getObservaciones())
                .fechaPago(pago.getFechaPago())
                .fechaCreacion(pago.getFechaCreacion())
                .fechaModificacion(pago.getFechaModificacion())
                .build();
    }
}
