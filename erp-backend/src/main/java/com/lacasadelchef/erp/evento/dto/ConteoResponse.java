package com.lacasadelchef.erp.evento.dto;

import lombok.Builder;

@Builder
public record ConteoResponse(
        String etiqueta,
        long cantidad
) {
}
