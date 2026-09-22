package com.lacasadelchef.erp.administracion.auditoria.dto;

import com.lacasadelchef.erp.entity.BitacoraAcceso;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BitacoraAccesoResponse(
        Long idBitacoraAcceso,
        Integer idUsuario,
        String usernameUsuario,
        String accion,
        String resultado,
        String ipOrigen,
        String navegador,
        String sesionId,
        LocalDateTime fechaAcceso
) {

    public static BitacoraAccesoResponse desde(BitacoraAcceso b) {
        return BitacoraAccesoResponse.builder()
                .idBitacoraAcceso(b.getIdBitacoraAcceso())
                .idUsuario(b.getUsuario() != null ? b.getUsuario().getIdUsuario() : null)
                .usernameUsuario(b.getUsuario() != null ? b.getUsuario().getUsername() : null)
                .accion(b.getAccion().getNombre())
                .resultado(b.getResultado())
                .ipOrigen(b.getIpOrigen())
                .navegador(b.getNavegador())
                .sesionId(b.getSesionId())
                .fechaAcceso(b.getFechaAcceso())
                .build();
    }
}
