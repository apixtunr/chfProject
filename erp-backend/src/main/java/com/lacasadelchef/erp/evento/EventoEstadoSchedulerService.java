package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Dispara PLANIFICADO -> EN CURSO -> FINALIZADO en el instante exacto de hora_inicio/
 * hora_fin de cada evento, en vez de esperar a la siguiente pasada de un job periodico.
 * Cada vez que un evento se crea, se edita o se cancela, hay que reprogramar su
 * temporizador (por eso EventoServiceImpl llama a programar()/cancelarTareas() en cada
 * uno de esos casos).
 *
 * Los temporizadores viven en memoria: si el backend se reinicia se pierden, por eso
 * al arrancar se reconstruyen todos a partir de lo que siga PLANIFICADO o EN CURSO en
 * la base de datos (reprogramarPendientes()). EventoEstadoAutomaticoJob se deja aparte
 * como red de seguridad de baja frecuencia, por si algun evento entra a la tabla sin
 * pasar por este servicio (por ejemplo, una carga directa por SQL).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventoEstadoSchedulerService {

    private static final String ESTADO_PLANIFICADO = "PLANIFICADO";
    private static final String ESTADO_EN_CURSO = "EN CURSO";

    private final TaskScheduler taskScheduler;
    private final EventoRepository eventoRepository;
    private final EventoEstadoTransicionExecutor executor;

    private final Map<Integer, ScheduledFuture<?>> temporizadoresInicio = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledFuture<?>> temporizadoresFin = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void reprogramarPendientes() {
        List<Evento> pendientes = eventoRepository.findByEstadoNombreIn(List.of(ESTADO_PLANIFICADO, ESTADO_EN_CURSO));
        pendientes.forEach(this::programar);
        if (!pendientes.isEmpty()) {
            log.info("Reprogramados {} temporizadores de estado de evento al arrancar", pendientes.size());
        }
    }

    /** Programa (o reprograma) el temporizador que corresponda segun el estado actual del evento. */
    public void programar(Evento evento) {
        Integer id = evento.getIdEvento();
        String estado = evento.getEstado().getNombre().toUpperCase();
        LocalDate fecha = evento.getFechaEvento();
        LocalTime horaInicio = evento.getHoraInicio();
        LocalTime horaFin = evento.getHoraFin();

        cancelar(temporizadoresInicio, id);
        cancelar(temporizadoresFin, id);

        if (ESTADO_PLANIFICADO.equals(estado)) {
            programarTarea(temporizadoresInicio, id, fecha, horaInicio, () -> {
                executor.pasarAEnCurso(id);
                programarTarea(temporizadoresFin, id, fecha, horaFin, () -> executor.pasarAFinalizado(id));
            });
        } else if (ESTADO_EN_CURSO.equals(estado)) {
            programarTarea(temporizadoresFin, id, fecha, horaFin, () -> executor.pasarAFinalizado(id));
        }
        // Cualquier otro estado (FINALIZADO, CANCELADO): no queda nada pendiente por programar.
    }

    /** Cancela cualquier temporizador pendiente de este evento (se cancelo o se elimino). */
    public void cancelarTareas(Integer idEvento) {
        cancelar(temporizadoresInicio, idEvento);
        cancelar(temporizadoresFin, idEvento);
    }

    private void programarTarea(Map<Integer, ScheduledFuture<?>> mapa, Integer idEvento,
                                 LocalDate fecha, LocalTime hora, Runnable tarea) {
        if (hora == null) {
            return;
        }
        var instante = LocalDateTime.of(fecha, hora).atZone(ZoneId.systemDefault()).toInstant();
        mapa.put(idEvento, taskScheduler.schedule(tarea, instante));
    }

    private void cancelar(Map<Integer, ScheduledFuture<?>> mapa, Integer idEvento) {
        ScheduledFuture<?> tarea = mapa.remove(idEvento);
        if (tarea != null) {
            tarea.cancel(false);
        }
    }
}
