package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mueve los eventos de PLANIFICADO a EN CURSO y de EN CURSO a FINALIZADO solos, en base a la
 * fecha/hora cargada, para que nadie tenga que acordarse de cambiarlo a mano. CANCELADO sigue
 * siendo siempre una decision manual (ver EventoServiceImpl.cambiarEstado).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventoEstadoAutomaticoJob {

    private static final String TIPO_ESTADO_EVENTO = "EVENTO";
    private static final String ESTADO_PLANIFICADO = "PLANIFICADO";
    private static final String ESTADO_EN_CURSO = "EN CURSO";
    private static final String ESTADO_FINALIZADO = "FINALIZADO";
    private static final String TABLA = "evento";

    private final EventoRepository eventoRepository;
    private final EstadoRepository estadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void actualizarEstados() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = ahora.toLocalDate();

        int iniciados = pasarEventos(ESTADO_PLANIFICADO, ESTADO_EN_CURSO, hoy, ahora, Evento::getHoraInicio);
        int finalizados = pasarEventos(ESTADO_EN_CURSO, ESTADO_FINALIZADO, hoy, ahora, Evento::getHoraFin);

        if (iniciados > 0 || finalizados > 0) {
            log.info("Job de estados de evento: {} pasados a EN CURSO, {} pasados a FINALIZADO", iniciados, finalizados);
        }
    }

    private int pasarEventos(String estadoOrigen, String estadoDestino, LocalDate hoy, LocalDateTime ahora,
                              java.util.function.Function<Evento, java.time.LocalTime> horaRelevante) {
        Estado nuevoEstado = null;
        int contador = 0;
        for (Evento evento : eventoRepository.findByEstadoNombreAndFechaEventoLessThanEqual(estadoOrigen, hoy)) {
            var hora = horaRelevante.apply(evento);
            if (hora == null) {
                continue;
            }
            LocalDateTime momento = LocalDateTime.of(evento.getFechaEvento(), hora);
            if (ahora.isBefore(momento)) {
                continue;
            }
            if (nuevoEstado == null) {
                nuevoEstado = buscarEstado(estadoDestino);
            }
            evento.setEstado(nuevoEstado);
            eventoRepository.save(evento);
            bitacoraMovimientoService.registrar(TABLA, evento.getIdEvento(), Operacion.UPDATE);
            contador++;
        }
        return contador;
    }

    private Estado buscarEstado(String nombre) {
        return estadoRepository.findByTipoEstadoNombreTipoAndNombre(TIPO_ESTADO_EVENTO, nombre)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el estado %s/%s (revisar datos semilla)".formatted(TIPO_ESTADO_EVENTO, nombre)));
    }
}
