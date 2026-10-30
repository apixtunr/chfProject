package com.lacasadelchef.erp.administracion.usuario;

import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class PoliticaUsuario {

    private final UsuarioRepository usuarioRepository;

    public String generarPara(Empleado empleado) {
        String base = base(empleado.getNombre(), empleado.getApellido());
        String candidato = base;
        for (int numero = 2; usuarioRepository.existsByUsernameIgnoreCase(candidato); numero++) {
            candidato = base + numero;
        }
        return candidato;
    }

    static String base(String nombres, String apellidos) {
        return primeraPalabra(nombres) + "." + primeraPalabra(apellidos);
    }

    private static String primeraPalabra(String texto) {
        String primera = texto == null ? "" : texto.trim().split("\\s+")[0];
        return Normalizer.normalize(primera, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }
}
