package com.lacasadelchef.erp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {
        String password = args.length > 0 ? args[0] : "changeme123";
        System.out.println(new BCryptPasswordEncoder().encode(password));
    }
}
