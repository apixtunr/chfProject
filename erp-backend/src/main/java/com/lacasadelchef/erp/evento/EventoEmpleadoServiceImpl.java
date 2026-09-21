package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.EventoEmpleado;
import com.lacasadelchef.erp.entity.id.EventoEmpleadoId;
import com.lacasadelchef.erp.evento.dto.EventoEmpleadoRequest;
import com.lacasadelchef.erp.evento.dto.EventoEmpleadoResponse;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.EventoEmpleadoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoEmpleadoServiceImpl implements EventoEmpleadoService {

    private static final String TABLA = "evento_empleado";
    private static final String TIPO_ESTADO_GENERAL = "GENERAL";
    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final EventoEmpleadoRepository eventoEmpleadoRepository;
    private final EventoRepository eventoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final EstadoRepository estadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<EventoEmpleadoResponse> listar(Integer idEvento) {
        return eventoEmpleadoRepository.findByEventoIdEvento(idEvento).stream()
                .map(EventoEmpleadoResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public EventoEmpleadoResponse asignar(Integer idEvento, Integer idEmpleado, EventoEmpleadoRequest request) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", idEvento));
        Empleado empleado = empleadoRepository.findById(idEmpleado)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado", idEmpleado));

        EventoEmpleado eventoEmpleado = new EventoEmpleado();
        eventoEmpleado.setId(new EventoEmpleadoId(idEvento, idEmpleado));
        eventoEmpleado.setEvento(evento);
        eventoEmpleado.setEmpleado(empleado);
        aplicar(request, eventoEmpleado);
        eventoEmpleado = eventoEmpleadoRepository.save(eventoEmpleado);
        bitacoraMovimientoService.registrar(TABLA, idEvento + "-" + idEmpleado, Operacion.INSERT);
        return EventoEmpleadoResponse.desde(eventoEmpleado);
    }

    @Override
    @Transactional
    public EventoEmpleadoResponse actualizar(Integer idEvento, Integer idEmpleado, EventoEmpleadoRequest request) {
        EventoEmpleado eventoEmpleado = buscar(idEvento, idEmpleado);
        aplicar(request, eventoEmpleado);
        eventoEmpleado = eventoEmpleadoRepository.save(eventoEmpleado);
        return EventoEmpleadoResponse.desde(eventoEmpleado);
    }

    @Override
    @Transactional
    public void quitar(Integer idEvento, Integer idEmpleado) {
        EventoEmpleado eventoEmpleado = buscar(idEvento, idEmpleado);
        eventoEmpleadoRepository.delete(eventoEmpleado);
        bitacoraMovimientoService.registrar(TABLA, idEvento + "-" + idEmpleado, Operacion.DELETE);
    }

    private EventoEmpleado buscar(Integer idEvento, Integer idEmpleado) {
        return eventoEmpleadoRepository.findById(new EventoEmpleadoId(idEvento, idEmpleado))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El empleado %d no esta asignado al evento %d".formatted(idEmpleado, idEvento)));
    }

    private void aplicar(EventoEmpleadoRequest request, EventoEmpleado eventoEmpleado) {
        Estado estado = request.idEstado() != null
                ? estadoRepository.findById(request.idEstado())
                        .orElseThrow(() -> new ResourceNotFoundException("Estado", request.idEstado()))
                : estadoRepository.findByTipoEstadoNombreTipoAndNombre(TIPO_ESTADO_GENERAL, ESTADO_ACTIVO)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No existe el estado %s/%s (revisar datos semilla)".formatted(TIPO_ESTADO_GENERAL, ESTADO_ACTIVO)));
        eventoEmpleado.setEstado(estado);
        eventoEmpleado.setSalarioEvento(request.salarioEvento());
        eventoEmpleado.setHoraInicio(request.horaInicio());
        eventoEmpleado.setHoraFin(request.horaFin());
    }
}
