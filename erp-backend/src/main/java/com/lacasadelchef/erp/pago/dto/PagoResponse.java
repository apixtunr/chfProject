package com.lacasadelchef.erp.pago.dto;

import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.Pago;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PagoResponse(
        Integer idPago,
        Integer idEvento,
        String clienteNombre,
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
        LocalDateTime fechaModificacion,
        Integer idCostoEvento
) {

    public static PagoResponse desde(Pago pago) {
        return PagoResponse.builder()
                .idPago(pago.getIdPago())
                .idEvento(pago.getEvento().getIdEvento())
                .clienteNombre(clienteDe(pago.getEvento()).getNombre())
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
                .idCostoEvento(pago.getCostoEvento() != null ? pago.getCostoEvento().getIdCostoEvento() : null)
                .build();
    }

    // El evento puede ser directo (cliente propio) o venir de una cotizacion (cliente ahi).
    private static Cliente clienteDe(Evento evento) {
        return evento.getCotizacionVersion() != null
                ? evento.getCotizacionVersion().getCotizacion().getCliente()
                : evento.getCliente();
    }
}
