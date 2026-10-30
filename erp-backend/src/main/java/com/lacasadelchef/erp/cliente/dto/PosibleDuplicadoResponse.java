package com.lacasadelchef.erp.cliente.dto;

import java.util.List;

public record PosibleDuplicadoResponse(
        Integer idCliente,
        String nombre,
        String nit,
        String telefono,
        String correo,
        boolean activo,
        List<String> coincidencias
) {
}
