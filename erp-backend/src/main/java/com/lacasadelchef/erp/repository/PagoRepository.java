package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByEventoIdEvento(Integer idEvento);

    Page<Pago> findByEventoIdEvento(Integer idEvento, Pageable pageable);
}
