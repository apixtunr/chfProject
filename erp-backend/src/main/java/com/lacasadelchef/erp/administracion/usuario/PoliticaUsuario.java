package com.lacasadelchef.erp.administracion.usuario;

import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Politica de nombres de usuario de la empresa: primer nombre, punto y primer apellido,
 * en minusculas y sin tildes, igual que los correos (ana.lopez@lacasadelchef.com). Ana
 * Judith Lopez Caceres entra como "ana.lopez". Si ya existe, se agrega un numero:
 * "ana.lopez2".
 *
 * El usuario lo genera el sistema a partir del empleado, nadie lo escribe: asi no hay
 * cuentas genericas como "admin" que no dicen quien es. Si despues se corrige el nombre
 * del empleado, su usuario no cambia, para no dejarlo sin poder entrar.
 */
@Component
@RequiredArgsConstructor
public class PoliticaUsuario {

    private final UsuarioRepository usuarioRepository;

    /** El primer usuario libre para este empleado segun la politica. */
    public String generarPara(Empleado empleado) {
        String base = base(empleado.getNombre(), empleado.getApellido());
        String candidato = base;
        for (int numero = 2; usuarioRepository.existsByUsernameIgnoreCase(candidato); numero++) {
            candidato = base + numero;
        }
        return candidato;
    }

    /** "Ana Judith" + "López Cáceres" -> "ana.lopez". */
    static String base(String nombres, String apellidos) {
        return primeraPalabra(nombres) + "." + primeraPalabra(apellidos);
    }

    private static String primeraPalabra(String texto) {
        String primera = texto == null ? "" : texto.trim().split("\\s+")[0];
        // Se separan las tildes de su letra (a + ´) y se quitan, igual la ñ; queda solo a-z y 0-9.
        return Normalizer.normalize(primera, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }
}
