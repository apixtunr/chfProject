package com.lacasadelchef.erp.evento.dto;

import com.lacasadelchef.erp.entity.CostoEvento;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record CostoEventoResponse(
        Integer idCostoEvento,
        Integer idEvento,
        Integer idTipoCosto,
        String tipoCostoNombre,
        String descripcion,
        BigDecimal monto,
        LocalDate fechaCosto,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static CostoEventoResponse desde(CostoEvento costoEvento) {
        return CostoEventoResponse.builder()
                .idCostoEvento(costoEvento.getIdCostoEvento())
                .idEvento(costoEvento.getEvento().getIdEvento())
                .idTipoCosto(costoEvento.getTipoCosto().getIdTipoCosto())
                .tipoCostoNombre(costoEvento.getTipoCosto().getNombreTipo())
                .descripcion(costoEvento.getDescripcion())
                .monto(costoEvento.getMonto())
                .fechaCosto(costoEvento.getFechaCosto())
                .fechaCreacion(costoEvento.getFechaCreacion())
                .fechaModificacion(costoEvento.getFechaModificacion())
                .build();
    }
}
