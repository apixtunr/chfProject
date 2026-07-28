package com.lacasadelchef.erp.pago.dto;

import com.lacasadelchef.erp.entity.ComprobantePago;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ComprobantePagoResponse(
        Integer idComprobante,
        Integer idPago,
        String numeroComprobante,
        String archivoUrl,
        String tipoComprobante,
        LocalDate fechaEmision,
        boolean esValido,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static ComprobantePagoResponse desde(ComprobantePago comprobante) {
        return ComprobantePagoResponse.builder()
                .idComprobante(comprobante.getIdComprobante())
                .idPago(comprobante.getPago().getIdPago())
                .numeroComprobante(comprobante.getNumeroComprobante())
                .archivoUrl(comprobante.getArchivoUrl())
                .tipoComprobante(comprobante.getTipoComprobante())
                .fechaEmision(comprobante.getFechaEmision())
                .esValido(comprobante.isEsValido())
                .fechaCreacion(comprobante.getFechaCreacion())
                .fechaModificacion(comprobante.getFechaModificacion())
                .build();
    }
}
