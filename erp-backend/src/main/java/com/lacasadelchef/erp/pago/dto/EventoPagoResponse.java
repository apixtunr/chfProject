package com.lacasadelchef.erp.pago.dto;

import com.lacasadelchef.erp.entity.VPagoEvento;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record EventoPagoResponse(
        Integer idEvento,
        LocalDate fechaEvento,
        Integer idEstado,
        String estadoNombre,
        String tipoEventoNombre,
        Integer idCliente,
        String clienteNombre,
        BigDecimal total,
        BigDecimal abonado,
        BigDecimal pendiente
) {

    public static EventoPagoResponse desde(VPagoEvento vista) {
        return EventoPagoResponse.builder()
                .idEvento(vista.getIdEvento())
                .fechaEvento(vista.getFechaEvento())
                .idEstado(vista.getIdEstado())
                .estadoNombre(vista.getEstadoNombre())
                .tipoEventoNombre(vista.getTipoEventoNombre())
                .idCliente(vista.getIdCliente())
                .clienteNombre(vista.getClienteNombre())
                .total(vista.getTotal())
                .abonado(vista.getAbonado())
                .pendiente(vista.getPendiente())
                .build();
    }
}
