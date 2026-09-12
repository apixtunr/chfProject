package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aplica en la base de datos el cambio de estado que dispara
 * {@link EventoEstadoSchedulerService}. Es un bean aparte (no metodos privados dentro
 * del scheduler) para que Spring pueda interceptar la llamada y aplicar @Transactional
 * de verdad: si el scheduler se llamara a si mismo, la anotacion se ignoraria en
 * silencio (auto-invocacion no pasa por el proxy de Spring).
 */
@Slf4j
@Component
@RequiredArgsConstructor
class EventoEstadoTransicionExecutor {

    private static final String TIPO_ESTADO_EVENTO = "EVENTO";
    private static final String ESTADO_PLANIFICADO = "PLANIFICADO";
    private static final String ESTADO_EN_CURSO = "EN CURSO";
    private static final String ESTADO_FINALIZADO = "FINALIZADO";
    private static final String TABLA = "evento";

    private final EventoRepository eventoRepository;
    private final EstadoRepository estadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;
    private final EventoInventarioService eventoInventarioService;

    @Transactional
    public void pasarAEnCurso(Integer idEvento) {
        eventoRepository.findById(idEvento).ifPresent(evento -> {
            // Si ya no esta PLANIFICADO (lo cancelaron, o algo mas ya lo movio), no se toca.
            if (!ESTADO_PLANIFICADO.equalsIgnoreCase(evento.getEstado().getNombre())) {
                return;
            }
            evento.setEstado(buscarEstado(ESTADO_EN_CURSO));
            eventoRepository.save(evento);
            bitacoraMovimientoService.registrar(TABLA, idEvento, Operacion.UPDATE);
            // Al iniciar el evento se da por hecho que ya no se cancelara antes de usarse: se
            // descuenta del inventario real todo lo planificado que aun seguia pendiente.
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
            bitacoraMovimientoService.registrar(TABLA, idEvento, Operacion.UPDATE);
            log.info("Evento {} paso a FINALIZADO en el instante exacto de hora_fin", idEvento);
        });
    }

    private Estado buscarEstado(String nombre) {
        return estadoRepository.findByTipoEstadoNombreTipoAndNombre(TIPO_ESTADO_EVENTO, nombre)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el estado %s/%s (revisar datos semilla)".formatted(TIPO_ESTADO_EVENTO, nombre)));
    }
}
