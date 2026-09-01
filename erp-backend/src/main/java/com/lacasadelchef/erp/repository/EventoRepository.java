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

    /** Evita que dos eventos distintos se creen a partir de la misma version de cotizacion. */
    boolean existsByCotizacionVersionIdCotizacionVersionAndIdEventoNot(Integer idCotizacionVersion, Integer idEvento);

    /**
     * Filtros opcionales para reportes (rentabilidad, agenda) y el dashboard de eventos.
     * Pasar null para omitir un filtro. LEFT JOIN porque un evento puede no tener cotizacion
     * (evento directo); el cliente en ese caso sale de e.cliente en vez de c.cliente.
     */
    @Query("""
            SELECT e FROM Evento e
            LEFT JOIN e.cotizacionVersion cv
            LEFT JOIN cv.cotizacion c
            WHERE (CAST(:fechaDesde AS date) IS NULL OR e.fechaEvento >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR e.fechaEvento <= :fechaHasta)
              AND (CAST(:idCliente AS integer) IS NULL OR c.cliente.idCliente = :idCliente OR e.cliente.idCliente = :idCliente)
              AND (CAST(:idTipoEvento AS integer) IS NULL OR e.tipoEvento.idTipoEvento = :idTipoEvento)
              AND (CAST(:idEstado AS integer) IS NULL OR e.estado.idEstado = :idEstado)
            """)
    Page<Evento> buscarPorFiltros(@Param("fechaDesde") LocalDate fechaDesde,
                                   @Param("fechaHasta") LocalDate fechaHasta,
                                   @Param("idCliente") Integer idCliente,
                                   @Param("idTipoEvento") Integer idTipoEvento,
                                   @Param("idEstado") Integer idEstado,
                                   Pageable pageable);

    /** Conteo de eventos agrupado por estado, para el dashboard. Mismos filtros que buscarPorFiltros. */
    @Query("""
            SELECT e.estado.nombre AS etiqueta, COUNT(e) AS cantidad
            FROM Evento e
            LEFT JOIN e.cotizacionVersion cv
            LEFT JOIN cv.cotizacion c
            WHERE (CAST(:fechaDesde AS date) IS NULL OR e.fechaEvento >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR e.fechaEvento <= :fechaHasta)
              AND (CAST(:idCliente AS integer) IS NULL OR c.cliente.idCliente = :idCliente OR e.cliente.idCliente = :idCliente)
              AND (CAST(:idTipoEvento AS integer) IS NULL OR e.tipoEvento.idTipoEvento = :idTipoEvento)
            GROUP BY e.estado.nombre
            """)
    List<ConteoProjection> contarPorEstado(@Param("fechaDesde") LocalDate fechaDesde,
                                            @Param("fechaHasta") LocalDate fechaHasta,
                                            @Param("idCliente") Integer idCliente,
                                            @Param("idTipoEvento") Integer idTipoEvento);

    /** Conteo de eventos agrupado por tipo de evento, para el dashboard. Mismos filtros que buscarPorFiltros. */
    @Query("""
            SELECT e.tipoEvento.nombreTipo AS etiqueta, COUNT(e) AS cantidad
            FROM Evento e
            LEFT JOIN e.cotizacionVersion cv
            LEFT JOIN cv.cotizacion c
            WHERE (CAST(:fechaDesde AS date) IS NULL OR e.fechaEvento >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR e.fechaEvento <= :fechaHasta)
              AND (CAST(:idCliente AS integer) IS NULL OR c.cliente.idCliente = :idCliente OR e.cliente.idCliente = :idCliente)
              AND (CAST(:idEstado AS integer) IS NULL OR e.estado.idEstado = :idEstado)
            GROUP BY e.tipoEvento.nombreTipo
            """)
    List<ConteoProjection> contarPorTipo(@Param("fechaDesde") LocalDate fechaDesde,
                                          @Param("fechaHasta") LocalDate fechaHasta,
                                          @Param("idCliente") Integer idCliente,
                                          @Param("idEstado") Integer idEstado);

    interface ConteoProjection {
        String getEtiqueta();
        Long getCantidad();
    }
}
