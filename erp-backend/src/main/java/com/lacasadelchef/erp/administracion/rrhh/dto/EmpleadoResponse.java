package com.lacasadelchef.erp.administracion.rrhh.dto;

import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Usuario;
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

        /**
         * Acceso al sistema de esta persona. Los tres van juntos: o tiene usuario y los
         * tres traen valor, o no tiene y los tres vienen vacios.
         *
         * Se resuelve consultando la tabla usuario, porque la llave de la relacion vive
         * de ese lado y la entidad Empleado no apunta de vuelta.
         */
        Integer idUsuario,
        String username,
        String rolUsuario,

        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    /** Sin datos de acceso; para cuando no hace falta consultarlos. */
    public static EmpleadoResponse desde(Empleado empleado) {
        return desde(empleado, null);
    }

    public static EmpleadoResponse desde(Empleado empleado, Usuario usuario) {
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
                .idUsuario(usuario != null ? usuario.getIdUsuario() : null)
                .username(usuario != null ? usuario.getUsername() : null)
                .rolUsuario(usuario != null ? usuario.getRol().getNombreRol() : null)
                .fechaCreacion(empleado.getFechaCreacion())
                .fechaModificacion(empleado.getFechaModificacion())
                .build();
    }
}
