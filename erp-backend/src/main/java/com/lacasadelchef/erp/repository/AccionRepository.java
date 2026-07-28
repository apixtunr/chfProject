package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Accion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccionRepository extends JpaRepository<Accion, Integer> {

    Optional<Accion> findByNombre(String nombre);
}
