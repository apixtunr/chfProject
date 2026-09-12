package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.EventoInventario;
import com.lacasadelchef.erp.entity.id.EventoInventarioId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoInventarioRepository extends JpaRepository<EventoInventario, EventoInventarioId> {

    List<EventoInventario> findByEventoIdEvento(Integer idEvento);

    List<EventoInventario> findByEventoIdEventoAndFechaConsumoIsNull(Integer idEvento);
}
