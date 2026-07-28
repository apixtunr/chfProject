package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.CostoEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CostoEventoRepository extends JpaRepository<CostoEvento, Integer> {

    List<CostoEvento> findByEventoIdEvento(Integer idEvento);
}
