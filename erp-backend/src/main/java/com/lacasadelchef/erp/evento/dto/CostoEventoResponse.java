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
        LocalDateTime fechaModificacion,
        /** true si algun Pago quedo enlazado a este costo (el cliente ya lo cubrio). */
        boolean pagado
) {

    /** Recien creado/actualizado nunca tiene pago enlazado todavia (eso lo crea una llamada aparte). */
    public static CostoEventoResponse desde(CostoEvento costoEvento) {
        return desde(costoEvento, false);
    }

    public static CostoEventoResponse desde(CostoEvento costoEvento, boolean pagado) {
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
                .pagado(pagado)
                .build();
    }
}
