package com.lacasadelchef.erp.cotizacion.dto;

import com.lacasadelchef.erp.entity.Cotizacion;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record CotizacionResponse(
        Integer idCotizacion,
        Integer idCliente,
        String clienteNombre,
        LocalDate fechaCotizacion,
        LocalDate fechaEvento,
        BigDecimal presupuestoCliente,
        Integer ultimaVersionId,
        Integer ultimaVersionNumero,
        String ultimaVersionEstado,
        BigDecimal ultimaVersionMonto,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    /** Para listados: incluye el estado/monto de la version mas reciente. */
    public static CotizacionResponse desde(Cotizacion cotizacion, CotizacionVersion ultimaVersion) {
        return CotizacionResponse.builder()
                .idCotizacion(cotizacion.getIdCotizacion())
                .idCliente(cotizacion.getCliente().getIdCliente())
                .clienteNombre(cotizacion.getCliente().getNombre())
                .fechaCotizacion(cotizacion.getFechaCotizacion())
                .fechaEvento(cotizacion.getFechaEvento())
                .presupuestoCliente(cotizacion.getPresupuestoCliente())
                .ultimaVersionId(ultimaVersion != null ? ultimaVersion.getIdCotizacionVersion() : null)
                .ultimaVersionNumero(ultimaVersion != null ? ultimaVersion.getNumeroVersion() : null)
                .ultimaVersionEstado(ultimaVersion != null ? ultimaVersion.getEstado().getNombre() : null)
                .ultimaVersionMonto(ultimaVersion != null ? ultimaVersion.getMontoTotal() : null)
                .fechaCreacion(cotizacion.getFechaCreacion())
                .fechaModificacion(cotizacion.getFechaModificacion())
                .build();
    }
}
