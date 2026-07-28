package com.lacasadelchef.erp.common.exception;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/** Estructura estandar de error que recibe el frontend. */
@Builder
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        String path,
        List<String> detalles
) {
}
