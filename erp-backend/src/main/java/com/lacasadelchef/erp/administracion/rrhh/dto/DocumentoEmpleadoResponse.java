package com.lacasadelchef.erp.administracion.rrhh.dto;

import com.lacasadelchef.erp.entity.DocumentoEmpleado;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DocumentoEmpleadoResponse(
        Integer idEmpleado,
        Integer idTipoDocumento,
        String tipoDocumentoNombre,
        String numeroDocumento,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static DocumentoEmpleadoResponse desde(DocumentoEmpleado documento) {
        return DocumentoEmpleadoResponse.builder()
                .idEmpleado(documento.getEmpleado().getIdEmpleado())
                .idTipoDocumento(documento.getTipoDocumento().getIdTipoDocumento())
                .tipoDocumentoNombre(documento.getTipoDocumento().getNombreTipo())
                .numeroDocumento(documento.getNumeroDocumento())
                .fechaCreacion(documento.getFechaCreacion())
                .fechaModificacion(documento.getFechaModificacion())
                .build();
    }
}
