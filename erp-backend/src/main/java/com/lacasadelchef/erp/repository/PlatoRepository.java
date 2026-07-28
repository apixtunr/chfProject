package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Plato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatoRepository extends JpaRepository<Plato, Integer> {

    Page<Plato> findByNombrePlatoContainingIgnoreCase(String nombrePlato, Pageable pageable);
}
