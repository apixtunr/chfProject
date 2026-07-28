package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    boolean existsByPlacaIgnoreCase(String placa);
}
