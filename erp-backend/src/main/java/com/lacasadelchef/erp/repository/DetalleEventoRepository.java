package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.DetalleEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleEventoRepository extends JpaRepository<DetalleEvento, Integer> {

    List<DetalleEvento> findByEventoIdEvento(Integer idEvento);

    boolean existsByEventoIdEvento(Integer idEvento);
}
