package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.EventoVehiculo;
import com.lacasadelchef.erp.entity.Vehiculo;
import com.lacasadelchef.erp.entity.id.EventoEmpleadoId;
import com.lacasadelchef.erp.entity.id.EventoVehiculoId;
import com.lacasadelchef.erp.evento.dto.EventoVehiculoRequest;
import com.lacasadelchef.erp.evento.dto.EventoVehiculoResponse;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.EventoEmpleadoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.EventoVehiculoRepository;
import com.lacasadelchef.erp.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoVehiculoServiceImpl implements EventoVehiculoService {

    private static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    private static final String ESTADO_ACTIVO = "ACTIVO";

    private final EventoVehiculoRepository eventoVehiculoRepository;
    private final EventoRepository eventoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final EventoEmpleadoRepository eventoEmpleadoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventoVehiculoResponse> listar(Integer idEvento) {
        return eventoVehiculoRepository.findByEventoIdEvento(idEvento).stream()
                .map(EventoVehiculoResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public EventoVehiculoResponse asignar(Integer idEvento, Integer idVehiculo, EventoVehiculoRequest request) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", idEvento));
        Vehiculo vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo", idVehiculo));
        EventoReglas.validarModificable(evento, "asignar vehículos a");
        if (!ESTADO_DISPONIBLE.equalsIgnoreCase(vehiculo.getEstado().getNombre())) {
            throw new BusinessException("El vehículo %s está %s y no se puede asignar"
                    .formatted(vehiculo.getPlaca(), vehiculo.getEstado().getNombre()));
        }
        if (eventoVehiculoRepository.existsById(new EventoVehiculoId(idEvento, idVehiculo))) {
            throw new BusinessException("El vehículo %s ya está asignado a este evento".formatted(vehiculo.getPlaca()));
        }
        // Un vehiculo hace un evento por dia: va, espera el servicio y regresa con el equipo.
        eventoVehiculoRepository.otrasAsignacionesDelDia(idVehiculo, idEvento, evento.getFechaEvento()).stream()
                .findFirst()
                .ifPresent(otra -> {
                    throw new BusinessException("El vehículo %s ya está asignado al evento #%d del mismo día"
                            .formatted(vehiculo.getPlaca(), otra.getEvento().getIdEvento()));
                });

        EventoVehiculo eventoVehiculo = new EventoVehiculo();
        eventoVehiculo.setId(new EventoVehiculoId(idEvento, idVehiculo));
        eventoVehiculo.setEvento(evento);
        eventoVehiculo.setVehiculo(vehiculo);
        aplicar(request, eventoVehiculo);
        eventoVehiculo = eventoVehiculoRepository.save(eventoVehiculo);
        return EventoVehiculoResponse.desde(eventoVehiculo);
    }

    @Override
    @Transactional
    public EventoVehiculoResponse actualizar(Integer idEvento, Integer idVehiculo, EventoVehiculoRequest request) {
        EventoVehiculo eventoVehiculo = buscar(idEvento, idVehiculo);
        EventoReglas.validarModificable(eventoVehiculo.getEvento(), "modificar los vehículos de");
        aplicar(request, eventoVehiculo);
        eventoVehiculo = eventoVehiculoRepository.save(eventoVehiculo);
        return EventoVehiculoResponse.desde(eventoVehiculo);
    }

    @Override
    @Transactional
    public void quitar(Integer idEvento, Integer idVehiculo) {
        EventoVehiculo eventoVehiculo = buscar(idEvento, idVehiculo);
        EventoReglas.validarModificable(eventoVehiculo.getEvento(), "quitar vehículos de");
        eventoVehiculoRepository.delete(eventoVehiculo);
    }

    private EventoVehiculo buscar(Integer idEvento, Integer idVehiculo) {
        return eventoVehiculoRepository.findById(new EventoVehiculoId(idEvento, idVehiculo))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El vehiculo %d no esta asignado al evento %d".formatted(idVehiculo, idEvento)));
    }

    private void aplicar(EventoVehiculoRequest request, EventoVehiculo eventoVehiculo) {
        Empleado conductor = null;
        if (request.idEmpleadoConductor() != null) {
            conductor = empleadoRepository.findById(request.idEmpleadoConductor())
                    .orElseThrow(() -> new ResourceNotFoundException("Empleado", request.idEmpleadoConductor()));
            if (!ESTADO_ACTIVO.equalsIgnoreCase(conductor.getEstado().getNombre())) {
                throw new BusinessException("%s está inactivo y no puede conducir".formatted(conductor.getNombreCompleto()));
            }
            // El conductor va con el equipo del evento: tiene que estar en su personal, asi su
            // pago y su horario quedan registrados y no se compromete en otro evento.
            Integer idEvento = eventoVehiculo.getEvento().getIdEvento();
            if (!eventoEmpleadoRepository.existsById(new EventoEmpleadoId(idEvento, conductor.getIdEmpleado()))) {
                throw new BusinessException("%s no está en el personal del evento: asígnelo primero en Personal para que pueda conducir"
                        .formatted(conductor.getNombreCompleto()));
            }
        }
        eventoVehiculo.setEmpleado(conductor);
    }
}
