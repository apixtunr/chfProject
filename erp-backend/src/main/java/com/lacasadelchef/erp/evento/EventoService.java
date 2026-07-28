package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.EventoRequest;
import com.lacasadelchef.erp.evento.dto.EventoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface EventoService {

    Page<EventoResponse> listar(LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable);

    EventoResponse obtenerPorId(Integer id);

    EventoResponse crear(EventoRequest request);

    EventoResponse actualizar(Integer id, EventoRequest request);

    void eliminar(Integer id);

    EventoResponse cambiarEstado(Integer id, Integer idEstado);
}
