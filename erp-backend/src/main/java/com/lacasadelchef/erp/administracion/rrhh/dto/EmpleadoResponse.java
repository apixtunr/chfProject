package com.lacasadelchef.erp.administracion.rrhh.dto;

import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Usuario;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

        /** Sin espacios; null en los empleados registrados antes de que fuera obligatorio. */
        String dpi,

        /** Los demas documentos. Solo viene al consultar un empleado, no en el listado. */
        List<DocumentoEmpleadoResponse> documentos,

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

    /** Sin datos de acceso ni documentos; para cuando no hace falta consultarlos. */
    public static EmpleadoResponse desde(Empleado empleado) {
        return desde(empleado, null, null, null);
    }

    /**
     * @param dpi        su numero de DPI, o null si no lo tiene registrado
     * @param documentos los demas documentos, o null si no se consultaron (listado)
     */
    public static EmpleadoResponse desde(Empleado empleado, Usuario usuario, String dpi,
                                         List<DocumentoEmpleadoResponse> documentos) {
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
                .dpi(dpi)
                .documentos(documentos)
                .idUsuario(usuario != null ? usuario.getIdUsuario() : null)
                .username(usuario != null ? usuario.getUsername() : null)
                .rolUsuario(usuario != null ? usuario.getRol().getNombreRol() : null)
                .fechaCreacion(empleado.getFechaCreacion())
                .fechaModificacion(empleado.getFechaModificacion())
                .build();
    }
}
