package com.lacasadelchef.erp.evento;

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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventoEstadoAutomaticoJob {

    private static final String TIPO_ESTADO_EVENTO = "EVENTO";
    private static final String ESTADO_CREADO = "CREADO";
    private static final String ESTADO_PLANIFICADO = "PLANIFICADO";
    private static final String ESTADO_EN_CURSO = "EN CURSO";
    private static final String ESTADO_FINALIZADO = "FINALIZADO";
    private static final String ESTADO_CANCELADO = "CANCELADO";

    private final EventoRepository eventoRepository;
    private final EstadoRepository estadoRepository;
    private final EventoInventarioService eventoInventarioService;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void actualizarEstados() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDate hoy = ahora.toLocalDate();

        List<Integer> cancelados = pasarEventos(ESTADO_CREADO, ESTADO_CANCELADO, hoy, ahora, Evento::getHoraInicio);
        List<Integer> iniciados = pasarEventos(ESTADO_PLANIFICADO, ESTADO_EN_CURSO, hoy, ahora, Evento::getHoraInicio);
        List<Integer> finalizados = pasarEventos(ESTADO_EN_CURSO, ESTADO_FINALIZADO, hoy, ahora, Evento::getHoraFin);

        iniciados.forEach(eventoInventarioService::confirmarConsumoAutomatico);

        if (!cancelados.isEmpty() || !iniciados.isEmpty() || !finalizados.isEmpty()) {
            log.info("Job de estados de evento: {} cancelados por no planificarse, {} pasados a EN CURSO, {} pasados a FINALIZADO",
                    cancelados.size(), iniciados.size(), finalizados.size());
        }
    }

    private List<Integer> pasarEventos(String estadoOrigen, String estadoDestino, LocalDate hoy, LocalDateTime ahora,
                                        java.util.function.Function<Evento, java.time.LocalTime> horaRelevante) {
        Estado nuevoEstado = null;
        List<Integer> transicionados = new ArrayList<>();
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
            transicionados.add(evento.getIdEvento());
        }
        return transicionados;
    }

    private Estado buscarEstado(String nombre) {
        return estadoRepository.findByTipoEstadoNombreTipoAndNombre(TIPO_ESTADO_EVENTO, nombre)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el estado %s/%s (revisar datos semilla)".formatted(TIPO_ESTADO_EVENTO, nombre)));
    }
}
