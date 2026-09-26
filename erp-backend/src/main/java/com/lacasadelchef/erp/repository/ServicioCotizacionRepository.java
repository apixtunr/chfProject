package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.ServicioCotizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioCotizacionRepository extends JpaRepository<ServicioCotizacion, Integer> {

    List<ServicioCotizacion> findByCotizacionVersionIdCotizacionVersion(Integer idCotizacionVersion);

    boolean existsByCotizacionVersionIdCotizacionVersion(Integer idCotizacionVersion);
}
