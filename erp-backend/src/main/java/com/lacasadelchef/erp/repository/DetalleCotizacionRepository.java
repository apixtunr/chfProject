package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.DetalleCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleCotizacionRepository extends JpaRepository<DetalleCotizacion, Integer> {

    List<DetalleCotizacion> findByCotizacionVersionIdCotizacionVersion(Integer idCotizacionVersion);
}
