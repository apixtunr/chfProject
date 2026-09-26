package com.lacasadelchef.erp.administracion.auditoria.dto;

import com.lacasadelchef.erp.entity.BitacoraMovimiento;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BitacoraMovimientoResponse(
        Long idBitacoraMovimiento,
        Integer idUsuario,
        String usernameUsuario,
        /** Persona detras del usuario ("Nombre Apellido"); quien hizo la accion. */
        String nombreEmpleado,
        String tablaAfectada,
        String registroId,
        String operacion,
        String nombreAtributo,
        String valorAnterior,
        String valorNuevo,
        String ipOrigen,
        LocalDateTime fechaMovimiento
) {

    public static BitacoraMovimientoResponse desde(BitacoraMovimiento b) {
        return BitacoraMovimientoResponse.builder()
                .idBitacoraMovimiento(b.getIdBitacoraMovimiento())
                .idUsuario(b.getUsuario() != null ? b.getUsuario().getIdUsuario() : null)
                .usernameUsuario(b.getUsuario() != null ? b.getUsuario().getUsername() : null)
                .nombreEmpleado(b.getUsuario() != null ? b.getUsuario().getEmpleado().getNombreCompleto() : null)
                .tablaAfectada(b.getTablaAfectada())
                .registroId(b.getRegistroId())
                .operacion(b.getOperacion())
                .nombreAtributo(b.getNombreAtributo())
                .valorAnterior(b.getValorAnterior())
                .valorNuevo(b.getValorNuevo())
                .ipOrigen(b.getIpOrigen())
                .fechaMovimiento(b.getFechaMovimiento())
                .build();
    }
}
