package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
class EventoEstadoTransicionExecutor {

    private static final String TIPO_ESTADO_EVENTO = "EVENTO";
    private static final String ESTADO_CREADO = "CREADO";
    private static final String ESTADO_PLANIFICADO = "PLANIFICADO";
    private static final String ESTADO_EN_CURSO = "EN CURSO";
    private static final String ESTADO_FINALIZADO = "FINALIZADO";
    private static final String ESTADO_CANCELADO = "CANCELADO";

    private final EventoRepository eventoRepository;
    private final EstadoRepository estadoRepository;
    private final EventoInventarioService eventoInventarioService;

    @Transactional
    public void pasarAEnCurso(Integer idEvento) {
        eventoRepository.findById(idEvento).ifPresent(evento -> {
            if (!ESTADO_PLANIFICADO.equalsIgnoreCase(evento.getEstado().getNombre())) {
                return;
            }
            evento.setEstado(buscarEstado(ESTADO_EN_CURSO));
            eventoRepository.save(evento);
            eventoInventarioService.confirmarConsumoAutomatico(idEvento);
            log.info("Evento {} paso a EN CURSO en el instante exacto de hora_inicio", idEvento);
        });
    }

    @Transactional
    public void pasarAFinalizado(Integer idEvento) {
        eventoRepository.findById(idEvento).ifPresent(evento -> {
            if (!ESTADO_EN_CURSO.equalsIgnoreCase(evento.getEstado().getNombre())) {
                return;
            }
            evento.setEstado(buscarEstado(ESTADO_FINALIZADO));
            eventoRepository.save(evento);
            log.info("Evento {} paso a FINALIZADO en el instante exacto de hora_fin", idEvento);
        });
    }

    @Transactional
    public void cancelarPorFaltaDePlanificacion(Integer idEvento) {
        eventoRepository.findById(idEvento).ifPresent(evento -> {
            if (!ESTADO_CREADO.equalsIgnoreCase(evento.getEstado().getNombre())) {
                return;
            }
            evento.setEstado(buscarEstado(ESTADO_CANCELADO));
            eventoRepository.save(evento);
            log.info("Evento {} paso a CANCELADO automaticamente: nunca se planifico antes de su hora de inicio",
                    idEvento);
        });
    }

    private Estado buscarEstado(String nombre) {
        return estadoRepository.findByTipoEstadoNombreTipoAndNombre(TIPO_ESTADO_EVENTO, nombre)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el estado %s/%s (revisar datos semilla)".formatted(TIPO_ESTADO_EVENTO, nombre)));
    }
}
