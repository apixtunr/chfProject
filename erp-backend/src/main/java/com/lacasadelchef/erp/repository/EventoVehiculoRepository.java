package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.EventoVehiculo;
import com.lacasadelchef.erp.entity.id.EventoVehiculoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoVehiculoRepository extends JpaRepository<EventoVehiculo, EventoVehiculoId> {

    List<EventoVehiculo> findByEventoIdEvento(Integer idEvento);

    boolean existsByEventoIdEvento(Integer idEvento);
}
