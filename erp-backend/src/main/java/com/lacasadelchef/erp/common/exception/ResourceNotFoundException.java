package com.lacasadelchef.erp.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String recurso, Object id) {
        super("%s con id %s no encontrado".formatted(recurso, id));
    }

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
