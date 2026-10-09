package com.lacasadelchef.erp.pago.dto;

import com.lacasadelchef.erp.entity.ComprobantePago;
import com.lacasadelchef.erp.repository.ComprobanteArchivoRepository.ArchivoResumen;
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

        /** Datos del archivo adjunto; los tres en null si no tiene. */
        String nombreArchivo,
        String tipoContenido,
        Integer tamanoBytes,

        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static ComprobantePagoResponse desde(ComprobantePago comprobante) {
        return desde(comprobante, null);
    }

    public static ComprobantePagoResponse desde(ComprobantePago comprobante, ArchivoResumen archivo) {
        return ComprobantePagoResponse.builder()
                .idComprobante(comprobante.getIdComprobante())
                .idPago(comprobante.getPago().getIdPago())
                .numeroComprobante(comprobante.getNumeroComprobante())
                .archivoUrl(comprobante.getArchivoUrl())
                .tipoComprobante(comprobante.getTipoComprobante())
                .fechaEmision(comprobante.getFechaEmision())
                .esValido(comprobante.isEsValido())
                .nombreArchivo(archivo != null ? archivo.getNombreArchivo() : null)
                .tipoContenido(archivo != null ? archivo.getTipoContenido() : null)
                .tamanoBytes(archivo != null ? archivo.getTamanoBytes() : null)
                .fechaCreacion(comprobante.getFechaCreacion())
                .fechaModificacion(comprobante.getFechaModificacion())
                .build();
    }
}
