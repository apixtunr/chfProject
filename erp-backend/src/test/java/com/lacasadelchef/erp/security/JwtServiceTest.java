package com.lacasadelchef.erp.security;

import com.lacasadelchef.erp.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "secreto-de-prueba-suficientemente-largo-para-hmac-sha-256-lacasadelchef";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(props(SECRET, 60));
    }

    @Test
    @DisplayName("El token generado es valido y conserva el username")
    void tokenValidoConservaUsername() {
        String token = jwtService.generarToken("admin", "ADMINISTRADOR");

        assertThat(jwtService.esValido(token)).isTrue();
        assertThat(jwtService.extraerUsername(token)).isEqualTo("admin");
    }

    @Test
    @DisplayName("Un token adulterado o basura no es valido")
    void tokenAdulteradoNoEsValido() {
        String token = jwtService.generarToken("admin", "ADMINISTRADOR");

        assertThat(jwtService.esValido(token + "x")).isFalse();
        assertThat(jwtService.esValido("no-es-un-jwt")).isFalse();
    }

    @Test
    @DisplayName("Un token firmado con otro secreto no es valido")
    void tokenDeOtroSecretoNoEsValido() {
        JwtService otroServicio = new JwtService(
                props("otro-secreto-diferente-tambien-largo-para-hmac-sha-256-lacasadelchef", 60));
        String tokenAjeno = otroServicio.generarToken("admin", "ADMINISTRADOR");

        assertThat(jwtService.esValido(tokenAjeno)).isFalse();
    }

    @Test
    @DisplayName("Un token expirado no es valido")
    void tokenExpiradoNoEsValido() {
        JwtService servicioExpirado = new JwtService(props(SECRET, -1)); // expiro hace 1 minuto
        String token = servicioExpirado.generarToken("admin", "ADMINISTRADOR");

        assertThat(jwtService.esValido(token)).isFalse();
    }

    private static AppProperties props(String secret, long expirationMinutes) {
        return new AppProperties(
                new AppProperties.Security(new AppProperties.Security.Jwt(secret, expirationMinutes), 5),
                new AppProperties.Cors(List.of()));
    }
}
