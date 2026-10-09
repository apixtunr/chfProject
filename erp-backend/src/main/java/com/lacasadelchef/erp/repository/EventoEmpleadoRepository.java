package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.EventoEmpleado;
import com.lacasadelchef.erp.entity.id.EventoEmpleadoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventoEmpleadoRepository extends JpaRepository<EventoEmpleado, EventoEmpleadoId> {

    List<EventoEmpleado> findByEventoIdEvento(Integer idEvento);

    boolean existsByEventoIdEvento(Integer idEvento);

    /** Donde mas trabaja ese empleado el mismo dia (sin contar eventos cancelados). */
    @Query("""
            SELECT ee FROM EventoEmpleado ee
            WHERE ee.empleado.idEmpleado = :idEmpleado
              AND ee.evento.idEvento <> :idEvento
              AND ee.evento.fechaEvento = :fecha
              AND ee.evento.estado.nombre <> 'CANCELADO'
            """)
    List<EventoEmpleado> otrasAsignacionesDelDia(@Param("idEmpleado") Integer idEmpleado,
                                                 @Param("idEvento") Integer idEvento,
                                                 @Param("fecha") LocalDate fecha);

    /** Lo que se le pago a cada persona en los eventos FINALIZADOS de un periodo, por persona y fecha. */
    @Query("""
            SELECT ee FROM EventoEmpleado ee
            JOIN FETCH ee.evento e
            JOIN FETCH ee.empleado emp
            LEFT JOIN FETCH emp.puestoEmpleado
            WHERE e.estado.nombre = 'FINALIZADO'
              AND (CAST(:fechaDesde AS date) IS NULL OR e.fechaEvento >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR e.fechaEvento <= :fechaHasta)
              AND (CAST(:idEmpleado AS integer) IS NULL OR emp.idEmpleado = :idEmpleado)
            ORDER BY emp.nombre, emp.apellido, e.fechaEvento, e.idEvento
            """)
    List<EventoEmpleado> pagosEnFinalizados(@Param("fechaDesde") LocalDate fechaDesde,
                                            @Param("fechaHasta") LocalDate fechaHasta,
                                            @Param("idEmpleado") Integer idEmpleado);
}
