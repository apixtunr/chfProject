package com.lacasadelchef.erp.cliente.dto;

import java.util.List;

/**
 * Un cliente ya registrado que se parece al que se esta capturando, y por que (mismo
 * NIT, telefono, correo o nombre). Salvo el NIT, que no se puede repetir, es solo un
 * aviso: el usuario confirma si se trata de otro cliente.
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
