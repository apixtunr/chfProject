package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Integer> {

    Page<Evento> findByFechaEventoBetween(LocalDate desde, LocalDate hasta, Pageable pageable);

    List<Evento> findByFechaEvento(LocalDate fecha);

    /** Para el job de estados automaticos: eventos de una fecha dada (o anterior) en cierto estado. */
    List<Evento> findByEstadoNombreAndFechaEventoLessThanEqual(String estadoNombre, LocalDate fecha);

    /** Para reconstruir los temporizadores de EventoEstadoSchedulerService al arrancar. */
    List<Evento> findByEstadoNombreIn(List<String> estadoNombres);

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

    /** Personas atendidas: la suma de invitados de los eventos FINALIZADOS. Mismos filtros que el resumen. */
    @Query("""
            SELECT COALESCE(SUM(e.cantidadPersonas), 0)
            FROM Evento e
            LEFT JOIN e.cotizacionVersion cv
            LEFT JOIN cv.cotizacion c
            WHERE e.estado.nombre = 'FINALIZADO'
              AND (CAST(:fechaDesde AS date) IS NULL OR e.fechaEvento >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR e.fechaEvento <= :fechaHasta)
              AND (CAST(:idCliente AS integer) IS NULL OR c.cliente.idCliente = :idCliente OR e.cliente.idCliente = :idCliente)
              AND (CAST(:idTipoEvento AS integer) IS NULL OR e.tipoEvento.idTipoEvento = :idTipoEvento)
            """)
    long sumarPersonasAtendidas(@Param("fechaDesde") LocalDate fechaDesde,
                                @Param("fechaHasta") LocalDate fechaHasta,
                                @Param("idCliente") Integer idCliente,
                                @Param("idTipoEvento") Integer idTipoEvento);

    /**
     * Rentabilidad de los eventos FINALIZADOS: solo cuando un evento termino estan completos
     * sus costos (el inventario se descuenta al iniciar y el personal se paga al final).
     *
     * Por evento: lo acordado (precio de su cotizacion o de su menu), lo abonado a ese precio,
     * los reembolsos de costos extra que pago el cliente (sin pagos anulados) y los costos
     * por rubro. El inventario se valora al precio actual del producto.
     */
    @Query(nativeQuery = true, value = """
            SELECT e.id_evento AS "idEvento",
                   e.fecha_evento AS "fechaEvento",
                   te.nombre_tipo AS "tipoEvento",
                   cl.id_cliente AS "idCliente",
                   cl.nombre AS "clienteNombre",
                   COALESCE(cv.monto_total, e.monto_menu, 0) AS "precio",
                   COALESCE((SELECT SUM(p.monto) FROM pago p JOIN estado sp ON sp.id_estado = p.id_estado
                             WHERE p.id_evento = e.id_evento AND p.id_costo_evento IS NULL
                               AND sp.nombre <> 'ANULADO'), 0) AS "abonado",
                   COALESCE((SELECT SUM(p.monto) FROM pago p JOIN estado sp ON sp.id_estado = p.id_estado
                             WHERE p.id_evento = e.id_evento AND p.id_costo_evento IS NOT NULL
                               AND sp.nombre <> 'ANULADO'), 0) AS "reembolsos",
                   COALESCE((SELECT SUM(ee.salario_evento) FROM evento_empleado ee
                             WHERE ee.id_evento = e.id_evento), 0) AS "costoPersonal",
                   COALESCE((SELECT SUM(ei.cantidad * pr.precio_unitario) FROM evento_inventario ei
                             JOIN producto pr ON pr.id_producto = ei.id_producto
                             WHERE ei.id_evento = e.id_evento AND ei.fecha_consumo IS NOT NULL), 0) AS "costoInventario",
                   COALESCE((SELECT SUM(c.monto) FROM costo_evento c
                             WHERE c.id_evento = e.id_evento), 0) AS "costoExtra"
            FROM evento e
            JOIN estado es ON es.id_estado = e.id_estado
            JOIN tipo_evento te ON te.id_tipo_evento = e.id_tipo_evento
            LEFT JOIN cotizacion_version cv ON cv.id_cotizacion_version = e.id_cotizacion_version
            LEFT JOIN cotizacion co ON co.id_cotizacion = cv.id_cotizacion
            LEFT JOIN cliente cl ON cl.id_cliente = COALESCE(co.id_cliente, e.id_cliente)
            WHERE es.nombre = 'FINALIZADO'
              AND (CAST(:idEvento AS integer) IS NULL OR e.id_evento = :idEvento)
              AND (CAST(:fechaDesde AS date) IS NULL OR e.fecha_evento >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR e.fecha_evento <= :fechaHasta)
              AND (CAST(:idCliente AS integer) IS NULL OR cl.id_cliente = :idCliente)
              AND (CAST(:idTipoEvento AS integer) IS NULL OR e.id_tipo_evento = :idTipoEvento)
            ORDER BY e.fecha_evento DESC, e.id_evento DESC
            """)
    List<RentabilidadFila> rentabilidadFinalizados(@Param("idEvento") Integer idEvento,
                                                   @Param("fechaDesde") LocalDate fechaDesde,
                                                   @Param("fechaHasta") LocalDate fechaHasta,
                                                   @Param("idCliente") Integer idCliente,
                                                   @Param("idTipoEvento") Integer idTipoEvento);

    interface RentabilidadFila {
        Integer getIdEvento();
        LocalDate getFechaEvento();
        String getTipoEvento();
        Integer getIdCliente();
        String getClienteNombre();
        BigDecimal getPrecio();
        BigDecimal getAbonado();
        BigDecimal getReembolsos();
        BigDecimal getCostoPersonal();
        BigDecimal getCostoInventario();
        BigDecimal getCostoExtra();
    }

    interface ConteoProjection {
        String getEtiqueta();
        Long getCantidad();
    }
}
