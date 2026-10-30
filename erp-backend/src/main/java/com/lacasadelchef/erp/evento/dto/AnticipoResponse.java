package com.lacasadelchef.erp.evento.dto;

import java.math.BigDecimal;

public record AnticipoResponse(
        BigDecimal total,
        BigDecimal abonado,
        BigDecimal anticipoRequerido,
        BigDecimal faltante,
        boolean cubierto
) {
}
