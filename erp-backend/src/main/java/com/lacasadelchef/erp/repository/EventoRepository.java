package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Integer> {

    Page<Evento> findByFechaEventoBetween(LocalDate desde, LocalDate hasta, Pageable pageable);

    List<Evento> findByFechaEvento(LocalDate fecha);

    /** Para el job de estados automaticos: eventos de una fecha dada (o anterior) en cierto estado. */
    List<Evento> findByEstadoNombreAndFechaEventoLessThanEqual(String estadoNombre, LocalDate fecha);

    /**
     * Filtros opcionales para reportes (rentabilidad, agenda). Pasar null para omitir un filtro.
     * LEFT JOIN porque un evento puede no tener cotizacion (evento directo); el cliente en ese
     * caso sale de e.cliente en vez de c.cliente.
     */
    @Query("""
            SELECT e FROM Evento e
            LEFT JOIN e.cotizacionVersion cv
            LEFT JOIN cv.cotizacion c
            WHERE (:fechaDesde IS NULL OR e.fechaEvento >= :fechaDesde)
              AND (:fechaHasta IS NULL OR e.fechaEvento <= :fechaHasta)
              AND (:idCliente IS NULL OR c.cliente.idCliente = :idCliente OR e.cliente.idCliente = :idCliente)
              AND (:idTipoEvento IS NULL OR e.tipoEvento.idTipoEvento = :idTipoEvento)
            """)
    Page<Evento> buscarPorFiltros(@Param("fechaDesde") LocalDate fechaDesde,
                                   @Param("fechaHasta") LocalDate fechaHasta,
                                   @Param("idCliente") Integer idCliente,
                                   @Param("idTipoEvento") Integer idTipoEvento,
                                   Pageable pageable);
}
