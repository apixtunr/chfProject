package com.lacasadelchef.erp.administracion.rrhh.dto;

import com.lacasadelchef.erp.entity.PuestoEmpleado;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PuestoEmpleadoResponse(
        Integer idPuestoEmpleado,
        String nombreRol,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static PuestoEmpleadoResponse desde(PuestoEmpleado puestoEmpleado) {
        return PuestoEmpleadoResponse.builder()
                .idPuestoEmpleado(puestoEmpleado.getIdPuestoEmpleado())
                .nombreRol(puestoEmpleado.getNombreRol())
                .fechaCreacion(puestoEmpleado.getFechaCreacion())
                .fechaModificacion(puestoEmpleado.getFechaModificacion())
                .build();
    }
}
