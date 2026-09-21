package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.EventoVehiculo;
import com.lacasadelchef.erp.entity.Vehiculo;
import com.lacasadelchef.erp.entity.id.EventoVehiculoId;
import com.lacasadelchef.erp.evento.dto.EventoVehiculoRequest;
import com.lacasadelchef.erp.evento.dto.EventoVehiculoResponse;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
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

    private static final String TABLA = "evento_vehiculo";

    private final EventoVehiculoRepository eventoVehiculoRepository;
    private final EventoRepository eventoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

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

        EventoVehiculo eventoVehiculo = new EventoVehiculo();
        eventoVehiculo.setId(new EventoVehiculoId(idEvento, idVehiculo));
        eventoVehiculo.setEvento(evento);
        eventoVehiculo.setVehiculo(vehiculo);
        aplicar(request, eventoVehiculo);
        eventoVehiculo = eventoVehiculoRepository.save(eventoVehiculo);
        bitacoraMovimientoService.registrar(TABLA, idEvento + "-" + idVehiculo, Operacion.INSERT);
        return EventoVehiculoResponse.desde(eventoVehiculo);
    }

    @Override
    @Transactional
    public EventoVehiculoResponse actualizar(Integer idEvento, Integer idVehiculo, EventoVehiculoRequest request) {
        EventoVehiculo eventoVehiculo = buscar(idEvento, idVehiculo);
        aplicar(request, eventoVehiculo);
        eventoVehiculo = eventoVehiculoRepository.save(eventoVehiculo);
        return EventoVehiculoResponse.desde(eventoVehiculo);
    }

    @Override
    @Transactional
    public void quitar(Integer idEvento, Integer idVehiculo) {
        EventoVehiculo eventoVehiculo = buscar(idEvento, idVehiculo);
        eventoVehiculoRepository.delete(eventoVehiculo);
        bitacoraMovimientoService.registrar(TABLA, idEvento + "-" + idVehiculo, Operacion.DELETE);
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
        }
        eventoVehiculo.setEmpleado(conductor);
    }
}
