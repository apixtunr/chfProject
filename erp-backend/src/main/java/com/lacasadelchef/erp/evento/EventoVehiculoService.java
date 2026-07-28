package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.EventoVehiculoRequest;
import com.lacasadelchef.erp.evento.dto.EventoVehiculoResponse;

import java.util.List;

public interface EventoVehiculoService {

    List<EventoVehiculoResponse> listar(Integer idEvento);

    EventoVehiculoResponse asignar(Integer idEvento, Integer idVehiculo, EventoVehiculoRequest request);

    EventoVehiculoResponse actualizar(Integer idEvento, Integer idVehiculo, EventoVehiculoRequest request);

    void quitar(Integer idEvento, Integer idVehiculo);
}
