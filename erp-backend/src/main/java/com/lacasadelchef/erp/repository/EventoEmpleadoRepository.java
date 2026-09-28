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
}
