package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.LineaVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LineaVehiculoRepository extends JpaRepository<LineaVehiculo, Integer> {

    List<LineaVehiculo> findByMarcaVehiculoIdMarcaVehiculo(Integer idMarcaVehiculo);
}
