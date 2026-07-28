package com.lacasadelchef.erp.common.exception;

/** Violaciones de reglas de negocio (devuelve HTTP 400). */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensaje) {
        super(mensaje);
    }
}
