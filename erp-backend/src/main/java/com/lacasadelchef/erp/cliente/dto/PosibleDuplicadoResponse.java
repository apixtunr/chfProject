package com.lacasadelchef.erp.cliente.dto;

import java.util.List;

/**
 * Un cliente ya registrado que se parece al que se esta capturando, y por que (mismo
 * NIT, telefono, correo o nombre). Es solo un aviso: el unico dato que bloquea el
 * guardado es el NIT repetido.
 */
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
