package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.cotizacion.dto.CotizacionResponse;
import com.lacasadelchef.erp.evento.dto.EventoRequest;
import com.lacasadelchef.erp.evento.dto.EventoResponse;
import com.lacasadelchef.erp.evento.dto.EventoResumenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface EventoService {

    Page<EventoResponse> listar(LocalDate fechaDesde, LocalDate fechaHasta, Integer idCliente,
                                 Integer idTipoEvento, Integer idEstado, Pageable pageable);

    /** Conteos por estado y por tipo de evento, para el dashboard. Mismos filtros que listar (sin idEstado). */
    EventoResumenResponse resumen(LocalDate fechaDesde, LocalDate fechaHasta, Integer idCliente, Integer idTipoEvento);

    EventoResponse obtenerPorId(Integer id);

    /** Cotizaciones ACEPTADA que todavia no tienen un evento asociado. */
    List<CotizacionResponse> listarCotizacionesDisponibles();

    EventoResponse crear(EventoRequest request);

    EventoResponse actualizar(Integer id, EventoRequest request);

    void eliminar(Integer id);

    EventoResponse cambiarEstado(Integer id, Integer idEstado);

    /**
     * Confirma que un evento CREADO ya esta listo para ejecutarse y lo pasa a PLANIFICADO
     * (recien ahi entra a la automatizacion de EN CURSO/FINALIZADO). Valida que ya tenga
     * Menu, Personal, Vehiculos e Inventario asignados; si falta alguno, rechaza el cambio.
     */
    EventoResponse planificar(Integer id);
}
