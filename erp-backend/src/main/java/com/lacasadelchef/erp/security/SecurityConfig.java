package com.lacasadelchef.erp.security;

import com.lacasadelchef.erp.config.AppProperties;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AutorizacionApi autorizacionApi;
    private final RespuestaSeguridad respuestaSeguridad;
    private final AppProperties appProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // API stateless con JWT: CSRF no aplica
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Los dos casos tienen que distinguirse, porque el frontend reacciona distinto:
            //   401 = no hay sesion valida (sin token, vencido o revocado) -> ir al login.
            //   403 = hay sesion, pero al rol le falta el permiso -> avisar, NO desloguear.
            // Sin el segundo manejador, Spring devolvia 401 tambien al denegar por permiso y
            // un usuario legitimo al que solo le faltaba un modulo terminaba expulsado.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(
                        (peticion, respuesta, causa) -> respuestaSeguridad.noAutenticado(peticion, respuesta))
                .accessDeniedHandler(
                        (peticion, respuesta, causa) -> respuestaSeguridad.sinPermiso(peticion, respuesta)))
            // Toda /api/** pasa por AutorizacionApi, que aplica la tabla de RecursosApi y
            // niega lo que no este declarado. Antes esto decia anyRequest().authenticated(),
            // que dejaba abierta cualquier consulta que nadie hubiera protegido a mano: asi
            // quedaron sin verificacion 90 de las 91 lecturas de la API.
            //
            // El orden importa: las reglas se evaluan de arriba abajo y gana la primera que
            // coincide, por eso el preflight de CORS y la documentacion van antes.
            .authorizeHttpRequests(auth -> auth
                // Cuando se responde un error, el contenedor reenvia la peticion a /error
                // para armar el cuerpo. Ese reenvio vuelve a pasar por aqui, y con el
                // denyAll() de abajo quedaba bloqueado: el 403 se perdia y el usuario
                // terminaba recibiendo un 401, o sea que lo mandaba al login en vez de
                // avisarle que le falta un permiso. No abre nada: son respuestas que el
                // propio servidor ya decidio emitir, no peticiones de nadie.
                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/**").access(autorizacionApi)
                .anyRequest().denyAll())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(appProperties.cors().allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
