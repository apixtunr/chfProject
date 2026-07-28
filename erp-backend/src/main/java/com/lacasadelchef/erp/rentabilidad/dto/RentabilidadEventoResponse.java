package com.lacasadelchef.erp.rentabilidad.dto;

import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.VRentabilidadEvento;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record RentabilidadEventoResponse(
        Integer idEvento,
        LocalDate fechaEvento,
        String tipoEventoNombre,
        Integer idCliente,
        String clienteNombre,
        BigDecimal totalIngresos,
        BigDecimal totalCostos,
        BigDecimal ganancia,
        BigDecimal porcentaje
) {

    public static RentabilidadEventoResponse desde(Evento evento, VRentabilidadEvento rentabilidad) {
        Cliente cliente = evento.getCotizacionVersion() != null
                ? evento.getCotizacionVersion().getCotizacion().getCliente()
                : evento.getCliente();
        return RentabilidadEventoResponse.builder()
                .idEvento(evento.getIdEvento())
                .fechaEvento(evento.getFechaEvento())
                .tipoEventoNombre(evento.getTipoEvento().getNombreTipo())
                .idCliente(cliente.getIdCliente())
                .clienteNombre(cliente.getNombre())
                .totalIngresos(rentabilidad != null ? rentabilidad.getTotalIngresos() : BigDecimal.ZERO)
                .totalCostos(rentabilidad != null ? rentabilidad.getTotalCostos() : BigDecimal.ZERO)
                .ganancia(rentabilidad != null ? rentabilidad.getGanancia() : BigDecimal.ZERO)
                .porcentaje(rentabilidad != null ? rentabilidad.getPorcentaje() : BigDecimal.ZERO)
                .build();
    }
}
