package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Evento;

import java.time.LocalTime;
import java.util.Set;

/** Reglas que comparten el evento y lo que se le asigna (personal, vehiculos). */
final class EventoReglas {

    static final String ESTADO_FINALIZADO = "FINALIZADO";
    static final String ESTADO_CANCELADO = "CANCELADO";

    /** Un evento que ya termino o se cancelo es historial: sus datos y asignaciones no cambian. */
    private static final Set<String> CERRADOS = Set.of(ESTADO_FINALIZADO, ESTADO_CANCELADO);

    private EventoReglas() {
    }

    static boolean estaCerrado(Evento evento) {
        return CERRADOS.contains(evento.getEstado().getNombre().toUpperCase());
    }

    /** "No se puede asignar personal a un evento FINALIZADO". */
    static void validarModificable(Evento evento, String accion) {
        if (estaCerrado(evento)) {
            throw new BusinessException("No se puede %s un evento %s"
                    .formatted(accion, evento.getEstado().getNombre()));
        }
    }

    /** Hora de fin posterior a la de inicio, si vienen las dos. */
    static void validarRango(LocalTime inicio, LocalTime fin, String quien) {
        if (inicio != null && fin != null && !fin.isAfter(inicio)) {
            throw new BusinessException("La hora de fin %s debe ser posterior a la de inicio".formatted(quien));
        }
    }

    /**
     * Si dos turnos del mismo dia se cruzan. Si a alguno le falta la hora no se puede
     * saber, y se toma como cruce: mejor preguntar que mandar a la misma persona a dos
     * lugares.
     */
    static boolean seCruzan(LocalTime inicioA, LocalTime finA, LocalTime inicioB, LocalTime finB) {
        if (inicioA == null || finA == null || inicioB == null || finB == null) {
            return true;
        }
        return inicioA.isBefore(finB) && inicioB.isBefore(finA);
    }
}
