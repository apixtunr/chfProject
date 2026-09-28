package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.EventoVehiculo;
import com.lacasadelchef.erp.entity.id.EventoVehiculoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EventoVehiculoRepository extends JpaRepository<EventoVehiculo, EventoVehiculoId> {

    List<EventoVehiculo> findByEventoIdEvento(Integer idEvento);

    boolean existsByEventoIdEvento(Integer idEvento);

    /** Otro evento del mismo dia que ya usa ese vehiculo (sin contar cancelados). */
    @Query("""
            SELECT ev FROM EventoVehiculo ev
            WHERE ev.vehiculo.idVehiculo = :idVehiculo
              AND ev.evento.idEvento <> :idEvento
              AND ev.evento.fechaEvento = :fecha
              AND ev.evento.estado.nombre <> 'CANCELADO'
            """)
    List<EventoVehiculo> otrasAsignacionesDelDia(@Param("idVehiculo") Integer idVehiculo,
                                                 @Param("idEvento") Integer idEvento,
                                                 @Param("fecha") LocalDate fecha);
}
