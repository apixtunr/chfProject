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

@Component
public class RespuestaSeguridad {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    public void noAutenticado(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        escribir(peticion, respuesta, HttpStatus.UNAUTHORIZED,
                "Su sesion no es valida o expiro. Vuelva a iniciar sesion.");
    }

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
