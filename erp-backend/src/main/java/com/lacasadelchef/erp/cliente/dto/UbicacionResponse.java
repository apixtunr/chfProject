package com.lacasadelchef.erp.cliente.dto;

import com.lacasadelchef.erp.entity.Ubicacion;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UbicacionResponse(
        Integer idUbicacion,
        Integer idCliente,
        String clienteNombre,
        Integer idMunicipio,
        String municipioNombre,
        String departamentoNombre,
        String direccion,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static UbicacionResponse desde(Ubicacion ubicacion) {
        return UbicacionResponse.builder()
                .idUbicacion(ubicacion.getIdUbicacion())
                .idCliente(ubicacion.getCliente() != null ? ubicacion.getCliente().getIdCliente() : null)
                .clienteNombre(ubicacion.getCliente() != null ? ubicacion.getCliente().getNombre() : null)
                .idMunicipio(ubicacion.getMunicipio().getIdMunicipio())
                .municipioNombre(ubicacion.getMunicipio().getNombreMunicipio())
                .departamentoNombre(ubicacion.getMunicipio().getDepartamento().getNombreDepartamento())
                .direccion(ubicacion.getDireccion())
                .fechaCreacion(ubicacion.getFechaCreacion())
                .fechaModificacion(ubicacion.getFechaModificacion())
                .build();
    }
}
