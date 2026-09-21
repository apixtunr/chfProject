package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.EventoEmpleado;
import com.lacasadelchef.erp.entity.id.EventoEmpleadoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoEmpleadoRepository extends JpaRepository<EventoEmpleado, EventoEmpleadoId> {

    List<EventoEmpleado> findByEventoIdEvento(Integer idEvento);

    boolean existsByEventoIdEvento(Integer idEvento);
}
