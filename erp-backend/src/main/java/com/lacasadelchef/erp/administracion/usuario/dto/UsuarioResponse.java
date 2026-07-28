package com.lacasadelchef.erp.administracion.usuario.dto;

import com.lacasadelchef.erp.entity.Usuario;
import lombok.Builder;

import java.time.LocalDateTime;

/** Nunca incluye passwordHash. */
@Builder
public record UsuarioResponse(
        Integer idUsuario,
        String username,
        Integer idRol,
        String nombreRol,
        Integer idEstado,
        String estadoNombre,
        Integer idEmpleado,
        String nombreEmpleado,
        Integer intentosAcceso,
        LocalDateTime fechaUltimoAcceso,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static UsuarioResponse desde(Usuario usuario) {
        return UsuarioResponse.builder()
                .idUsuario(usuario.getIdUsuario())
                .username(usuario.getUsername())
                .idRol(usuario.getRol().getIdRol())
                .nombreRol(usuario.getRol().getNombreRol())
                .idEstado(usuario.getEstado().getIdEstado())
                .estadoNombre(usuario.getEstado().getNombre())
                .idEmpleado(usuario.getEmpleado() != null ? usuario.getEmpleado().getIdEmpleado() : null)
                .nombreEmpleado(usuario.getEmpleado() != null
                        ? usuario.getEmpleado().getNombre() + " " + usuario.getEmpleado().getApellido()
                        : null)
                .intentosAcceso(usuario.getIntentosAcceso())
                .fechaUltimoAcceso(usuario.getFechaUltimoAcceso())
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaModificacion(usuario.getFechaModificacion())
                .build();
    }
}
