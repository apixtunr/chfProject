package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.EventoEmpleadoRequest;
import com.lacasadelchef.erp.evento.dto.EventoEmpleadoResponse;

import java.util.List;

public interface EventoEmpleadoService {

    List<EventoEmpleadoResponse> listar(Integer idEvento);

    EventoEmpleadoResponse asignar(Integer idEvento, Integer idEmpleado, EventoEmpleadoRequest request);

    EventoEmpleadoResponse actualizar(Integer idEvento, Integer idEmpleado, EventoEmpleadoRequest request);

    void quitar(Integer idEvento, Integer idEmpleado);
}
