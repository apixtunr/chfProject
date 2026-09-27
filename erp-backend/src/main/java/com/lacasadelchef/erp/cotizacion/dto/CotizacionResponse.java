package com.lacasadelchef.erp.cotizacion.dto;

import com.lacasadelchef.erp.cotizacion.CondicionesComerciales;
import com.lacasadelchef.erp.entity.Cotizacion;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
public record CotizacionResponse(
        Integer idCotizacion,
        Integer idCliente,
        String clienteNombre,
        Integer idTipoEvento,
        String tipoEventoNombre,
        Integer idUbicacion,
        String direccionUbicacion,
        Integer cantidadPersonas,
        LocalDate fechaCotizacion,
        LocalDate fechaEvento,
        BigDecimal presupuestoCliente,
        LocalTime horaInicio,
        /** Calculada: 4 horas despues del inicio, sin pasar del cierre del servicio. */
        LocalTime horaFin,
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
                .idTipoEvento(cotizacion.getTipoEvento().getIdTipoEvento())
                .tipoEventoNombre(cotizacion.getTipoEvento().getNombreTipo())
                .idUbicacion(cotizacion.getUbicacion().getIdUbicacion())
                .direccionUbicacion(cotizacion.getUbicacion().getDireccion())
                .cantidadPersonas(cotizacion.getCantidadPersonas())
                .fechaCotizacion(cotizacion.getFechaCotizacion())
                .fechaEvento(cotizacion.getFechaEvento())
                .presupuestoCliente(cotizacion.getPresupuestoCliente())
                .horaInicio(cotizacion.getHoraInicio())
                .horaFin(cotizacion.getHoraInicio() == null ? null
                        : CondicionesComerciales.horaFinServicio(cotizacion.getHoraInicio()))
                .ultimaVersionId(ultimaVersion != null ? ultimaVersion.getIdCotizacionVersion() : null)
                .ultimaVersionNumero(ultimaVersion != null ? ultimaVersion.getNumeroVersion() : null)
                .ultimaVersionEstado(ultimaVersion != null ? ultimaVersion.getEstado().getNombre() : null)
                .ultimaVersionMonto(ultimaVersion != null ? ultimaVersion.getMontoTotal() : null)
                .fechaCreacion(cotizacion.getFechaCreacion())
                .fechaModificacion(cotizacion.getFechaModificacion())
                .build();
    }
}
