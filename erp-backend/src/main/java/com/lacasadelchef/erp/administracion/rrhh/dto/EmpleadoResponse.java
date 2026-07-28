package com.lacasadelchef.erp.administracion.rrhh.dto;

import com.lacasadelchef.erp.entity.Empleado;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record EmpleadoResponse(
        Integer idEmpleado,
        Integer idPuestoEmpleado,
        String puestoNombre,
        Integer idEstado,
        String estadoNombre,
        Integer idGenero,
        String generoNombre,
        String nombre,
        String apellido,
        String correo,
        String telefono,
        LocalDate fechaContratacion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static EmpleadoResponse desde(Empleado empleado) {
        return EmpleadoResponse.builder()
                .idEmpleado(empleado.getIdEmpleado())
                .idPuestoEmpleado(empleado.getPuestoEmpleado().getIdPuestoEmpleado())
                .puestoNombre(empleado.getPuestoEmpleado().getNombreRol())
                .idEstado(empleado.getEstado().getIdEstado())
                .estadoNombre(empleado.getEstado().getNombre())
                .idGenero(empleado.getGenero() != null ? empleado.getGenero().getIdGenero() : null)
                .generoNombre(empleado.getGenero() != null ? empleado.getGenero().getNombreGenero() : null)
                .nombre(empleado.getNombre())
                .apellido(empleado.getApellido())
                .correo(empleado.getCorreo())
                .telefono(empleado.getTelefono())
                .fechaContratacion(empleado.getFechaContratacion())
                .fechaCreacion(empleado.getFechaCreacion())
                .fechaModificacion(empleado.getFechaModificacion())
                .build();
    }
}
