package com.lacasadelchef.erp.cliente.dto;

import com.lacasadelchef.erp.entity.Cliente;
import lombok.Builder;

import java.time.LocalDateTime;

/** Datos de salida de un cliente (nunca se expone la entidad directamente). */
@Builder
public record ClienteResponse(
        Integer idCliente,
        String nombre,
        String correo,
        String telefono,
        String nit,
        String direccion,
        Integer idMunicipio,
        String nombreMunicipio,
        Integer idDepartamento,
        String nombreDepartamento,
        Integer idEstado,
        String estadoNombre,
        boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {

    public static ClienteResponse desde(Cliente cliente) {
        return ClienteResponse.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .correo(cliente.getCorreo())
                .telefono(cliente.getTelefono())
                .nit(cliente.getNit())
                .direccion(cliente.getDireccion())
                .idMunicipio(cliente.getMunicipio().getIdMunicipio())
                .nombreMunicipio(cliente.getMunicipio().getNombreMunicipio())
                .idDepartamento(cliente.getMunicipio().getDepartamento().getIdDepartamento())
                .nombreDepartamento(cliente.getMunicipio().getDepartamento().getNombreDepartamento())
                .idEstado(cliente.getEstado().getIdEstado())
                .estadoNombre(cliente.getEstado().getNombre())
                .activo(cliente.estaActivo())
                .fechaCreacion(cliente.getFechaCreacion())
                .fechaModificacion(cliente.getFechaModificacion())
                .build();
    }
}
