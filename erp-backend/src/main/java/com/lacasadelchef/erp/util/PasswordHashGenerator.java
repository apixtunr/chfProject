package com.lacasadelchef.erp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad para generar hashes BCrypt (crear el primer usuario admin).
 * Ejecutar desde el IDE: Run 'PasswordHashGenerator.main()'.
 * No forma parte de la aplicacion en ejecucion.
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        String password = args.length > 0 ? args[0] : "changeme123";
        System.out.println(new BCryptPasswordEncoder().encode(password));
    }
}
