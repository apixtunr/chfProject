package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Evento;

import java.time.LocalTime;
import java.util.Set;

final class EventoReglas {

    static final String ESTADO_FINALIZADO = "FINALIZADO";
    static final String ESTADO_CANCELADO = "CANCELADO";

    private static final Set<String> CERRADOS = Set.of(ESTADO_FINALIZADO, ESTADO_CANCELADO);

    private EventoReglas() {
    }

    static boolean estaCerrado(Evento evento) {
        return CERRADOS.contains(evento.getEstado().getNombre().toUpperCase());
    }

    static void validarModificable(Evento evento, String accion) {
        if (estaCerrado(evento)) {
            throw new BusinessException("No se puede %s un evento %s"
                    .formatted(accion, evento.getEstado().getNombre()));
        }
    }

    static void validarRango(LocalTime inicio, LocalTime fin, String quien) {
        if (inicio != null && fin != null && !fin.isAfter(inicio)) {
            throw new BusinessException("La hora de fin %s debe ser posterior a la de inicio".formatted(quien));
        }
    }

    static boolean seCruzan(LocalTime inicioA, LocalTime finA, LocalTime inicioB, LocalTime finB) {
        if (inicioA == null || finA == null || inicioB == null || finB == null) {
            return true;
        }
        return inicioA.isBefore(finB) && inicioB.isBefore(finA);
    }
}
