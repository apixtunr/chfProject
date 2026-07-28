package com.lacasadelchef.erp.administracion.geografia.dto;

import com.lacasadelchef.erp.entity.Departamento;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DepartamentoResponse(
        Integer idDepartamento,
        String nombreDepartamento,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static DepartamentoResponse desde(Departamento departamento) {
        return DepartamentoResponse.builder()
                .idDepartamento(departamento.getIdDepartamento())
                .nombreDepartamento(departamento.getNombreDepartamento())
                .fechaCreacion(departamento.getFechaCreacion())
                .fechaModificacion(departamento.getFechaModificacion())
                .build();
    }
}
