package com.lacasadelchef.erp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lacasadelchef.erp.common.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Escribe los rechazos de seguridad con la misma forma que el resto de los errores de la
 * API (el record {@link ApiError}).
 *
 * Hace falta porque el interceptor del frontend muestra el campo "mensaje", y la pagina
 * de error que arma el contenedor por su cuenta no lo trae: al usuario le aparecia
 * "Ocurrio un error inesperado" en vez de saber que le falta un permiso.
 *
 * Ademas, escribir el cuerpo aca evita el reenvio interno a /error, asi que la respuesta
 * sale tal cual se decidio y nada la puede volver a procesar.
 */
@Component
public class RespuestaSeguridad {

    /**
     * Serializador propio, no el de la aplicacion.
     *
     * No se puede inyectar el ObjectMapper de Spring: SecurityConfig se construye muy
     * temprano (define el PasswordEncoder, que otros servicios necesitan) y para entonces
     * Jackson todavia no esta configurado, asi que la aplicacion no arrancaba. Con uno
     * propio esta clase no depende de ese orden.
     *
     * Se le registra el modulo de fechas y se desactivan las marcas numericas para que el
     * campo timestamp salga en texto ISO, igual que en los demas errores de la API.
     */
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    /** 401: no hay sesion valida. El frontend lleva al login. */
    public void noAutenticado(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        escribir(peticion, respuesta, HttpStatus.UNAUTHORIZED,
                "Su sesion no es valida o expiro. Vuelva a iniciar sesion.");
    }

    /** 403: hay sesion, pero al rol le falta el permiso. El frontend avisa y no desloguea. */
    public void sinPermiso(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        escribir(peticion, respuesta, HttpStatus.FORBIDDEN,
                "Su rol no tiene permiso para acceder a esta informacion.");
    }

    private void escribir(HttpServletRequest peticion, HttpServletResponse respuesta,
                          HttpStatus estado, String mensaje) throws IOException {
        if (respuesta.isCommitted()) {
            return;
        }
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(respuesta.getWriter(), ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(estado.value())
                .error(estado.getReasonPhrase())
                .mensaje(mensaje)
                .path(peticion.getRequestURI())
                .build());
    }
}
