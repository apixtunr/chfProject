package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.exception.BusinessException;
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
import com.lacasadelchef.erp.repository.EventoVehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoEmpleadoServiceImpl implements EventoEmpleadoService {

    private static final String TIPO_ESTADO_GENERAL = "GENERAL";
    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final EventoEmpleadoRepository eventoEmpleadoRepository;
    private final EventoRepository eventoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final EstadoRepository estadoRepository;
    private final EventoVehiculoRepository eventoVehiculoRepository;

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
        EventoReglas.validarModificable(evento, "asignar personal a");
        if (!ESTADO_ACTIVO.equalsIgnoreCase(empleado.getEstado().getNombre())) {
            throw new BusinessException("%s está inactivo y no se puede asignar a un evento"
                    .formatted(empleado.getNombreCompleto()));
        }
        if (eventoEmpleadoRepository.existsById(new EventoEmpleadoId(idEvento, idEmpleado))) {
            throw new BusinessException("%s ya está asignado a este evento".formatted(empleado.getNombreCompleto()));
        }

        EventoEmpleado eventoEmpleado = new EventoEmpleado();
        eventoEmpleado.setId(new EventoEmpleadoId(idEvento, idEmpleado));
        eventoEmpleado.setEvento(evento);
        eventoEmpleado.setEmpleado(empleado);
        aplicar(request, eventoEmpleado);
        eventoEmpleado = eventoEmpleadoRepository.save(eventoEmpleado);
        return EventoEmpleadoResponse.desde(eventoEmpleado);
    }

    @Override
    @Transactional
    public EventoEmpleadoResponse actualizar(Integer idEvento, Integer idEmpleado, EventoEmpleadoRequest request) {
        EventoEmpleado eventoEmpleado = buscar(idEvento, idEmpleado);
        // El salario se puede ajustar despues del evento (el gerente paga al terminar),
        // salvo en un evento cancelado.
        if (EventoReglas.ESTADO_CANCELADO.equalsIgnoreCase(eventoEmpleado.getEvento().getEstado().getNombre())) {
            throw new BusinessException("No se puede modificar el personal de un evento CANCELADO");
        }
        aplicar(request, eventoEmpleado);
        eventoEmpleado = eventoEmpleadoRepository.save(eventoEmpleado);
        return EventoEmpleadoResponse.desde(eventoEmpleado);
    }

    @Override
    @Transactional
    public void quitar(Integer idEvento, Integer idEmpleado) {
        EventoEmpleado eventoEmpleado = buscar(idEvento, idEmpleado);
        EventoReglas.validarModificable(eventoEmpleado.getEvento(), "quitar personal de");
        // Si conduce un vehiculo del evento, quitarlo dejaria al vehiculo con un conductor
        // que ya no forma parte del personal.
        eventoVehiculoRepository.findByEventoIdEvento(idEvento).stream()
                .filter(ev -> ev.getEmpleado() != null && ev.getEmpleado().getIdEmpleado().equals(idEmpleado))
                .findFirst()
                .ifPresent(ev -> {
                    throw new BusinessException("%s conduce el vehículo %s en este evento: cambie el conductor o quite el vehículo primero"
                            .formatted(ev.getEmpleado().getNombreCompleto(), ev.getVehiculo().getPlaca()));
                });
        eventoEmpleadoRepository.delete(eventoEmpleado);
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
        EventoReglas.validarRango(eventoEmpleado.getHoraInicio(), eventoEmpleado.getHoraFin(), "del turno");
        validarDisponible(eventoEmpleado);
    }

    /**
     * Una persona no puede estar en dos eventos a la vez. Se compara el turno de cada
     * asignacion (o el horario del evento si no se indico turno). No se exige que el turno
     * quede dentro del horario del evento: el personal llega antes a montar el buffet.
     */
    private void validarDisponible(EventoEmpleado asignacion) {
        Evento evento = asignacion.getEvento();
        LocalTime inicio = horaInicio(asignacion);
        LocalTime fin = horaFin(asignacion);
        for (EventoEmpleado otra : eventoEmpleadoRepository.otrasAsignacionesDelDia(
                asignacion.getEmpleado().getIdEmpleado(), evento.getIdEvento(), evento.getFechaEvento())) {
            if (EventoReglas.seCruzan(inicio, fin, horaInicio(otra), horaFin(otra))) {
                throw new BusinessException("%s ya está asignado al evento #%d del mismo día (%s)"
                        .formatted(asignacion.getEmpleado().getNombreCompleto(), otra.getEvento().getIdEvento(),
                                turno(otra)));
            }
        }
    }

    private static LocalTime horaInicio(EventoEmpleado a) {
        return a.getHoraInicio() != null ? a.getHoraInicio() : a.getEvento().getHoraInicio();
    }

    private static LocalTime horaFin(EventoEmpleado a) {
        return a.getHoraFin() != null ? a.getHoraFin() : a.getEvento().getHoraFin();
    }

    private static String turno(EventoEmpleado a) {
        LocalTime inicio = horaInicio(a);
        LocalTime fin = horaFin(a);
        return inicio == null || fin == null ? "sin horario definido" : "de %s a %s".formatted(inicio, fin);
    }
}
