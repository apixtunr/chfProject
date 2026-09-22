package com.lacasadelchef.erp.security;

import com.lacasadelchef.erp.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(AppProperties appProperties) {
        AppProperties.Security.Jwt jwt = appProperties.security().jwt();
        this.key = Keys.hmacShaKeyFor(jwt.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = jwt.expirationMinutes();
    }

    /**
     * idSesion identifica este inicio de sesion concreto y viaja dentro del token, para
     * poder emparejar despues en la bitacora el LOGIN con su LOGOUT (y ver cuales
     * quedaron abiertas). No se guarda en el servidor: la autenticacion sigue siendo
     * sin estado, el identificador solo acompana al token.
     */
    public String generarToken(String username, String rol, String idSesion) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .claim("sid", idSesion)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    /** Identificador de la sesion que abrio este token (claim sid). */
    public String extraerIdSesion(String token) {
        return extraerClaims(token).get("sid", String.class);
    }

    /** Fecha de emision del token (claim iat), usada para la revocacion por logout. */
    public Instant extraerFechaEmision(String token) {
        Date emision = extraerClaims(token).getIssuedAt();
        return emision != null ? emision.toInstant() : Instant.EPOCH;
    }

    public boolean esValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
