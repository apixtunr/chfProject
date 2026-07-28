package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoRepository extends JpaRepository<Estado, Integer> {

    Optional<Estado> findByTipoEstadoNombreTipoAndNombre(String nombreTipo, String nombre);
}
