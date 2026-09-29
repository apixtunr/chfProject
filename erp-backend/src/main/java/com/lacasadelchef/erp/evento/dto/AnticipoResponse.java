package com.lacasadelchef.erp.evento.dto;

import java.math.BigDecimal;

/**
 * Como va el primer pago de un evento: la empresa cobra el 50% una semana antes y el
 * resto al finalizar. "faltante" es lo que falta para llegar al 50% (0 si ya se cubrio).
 */
public record AnticipoResponse(
        BigDecimal total,
        BigDecimal abonado,
        BigDecimal anticipoRequerido,
        BigDecimal faltante,
        boolean cubierto
) {
}
