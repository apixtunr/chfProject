package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.CotizacionVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.query.Param;

public interface CotizacionVersionRepository extends JpaRepository<CotizacionVersion, Integer> {

    List<CotizacionVersion> findByCotizacionIdCotizacionOrderByNumeroVersionDesc(Integer idCotizacion);

    /** Para vencer: versiones en el estado indicado cuya vigencia termino antes de la fecha. */
    List<CotizacionVersion> findByEstadoNombreAndVigenteHastaBefore(String estado, LocalDate fecha);

    /** true si alguna version de la cotizacion ya salio del estado indicado (ej. ya se envio). */
    boolean existsByCotizacionIdCotizacionAndEstadoNombreNot(Integer idCotizacion, String estado);

    /** Candidatas para crear un evento: aceptadas y que ningun evento haya tomado todavia. */
    @Query("""
            SELECT cv FROM CotizacionVersion cv
            WHERE cv.estado.nombre = 'ACEPTADA'
              AND NOT EXISTS (SELECT 1 FROM Evento e WHERE e.cotizacionVersion = cv)
            ORDER BY cv.fechaVersion DESC
            """)
    List<CotizacionVersion> buscarAceptadasSinEvento();

    /**
     * Reporte de cotizaciones: la version vigente (la de numero mas alto) de cada cotizacion
     * hecha en el periodo, con su estado y su monto.
     */
    @Query("""
            SELECT v FROM CotizacionVersion v
            JOIN FETCH v.cotizacion c
            JOIN FETCH c.cliente
            JOIN FETCH c.tipoEvento
            JOIN FETCH v.estado
            WHERE v.numeroVersion = (SELECT MAX(v2.numeroVersion) FROM CotizacionVersion v2 WHERE v2.cotizacion = c)
              AND (CAST(:fechaDesde AS date) IS NULL OR c.fechaCotizacion >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR c.fechaCotizacion <= :fechaHasta)
              AND (CAST(:idEstado AS integer) IS NULL OR v.estado.idEstado = :idEstado)
              AND (CAST(:idCliente AS integer) IS NULL OR c.cliente.idCliente = :idCliente)
            ORDER BY c.fechaCotizacion, c.idCotizacion
            """)
    List<CotizacionVersion> reporteCotizaciones(@Param("fechaDesde") LocalDate fechaDesde,
                                                @Param("fechaHasta") LocalDate fechaHasta,
                                                @Param("idEstado") Integer idEstado,
                                                @Param("idCliente") Integer idCliente);
}
