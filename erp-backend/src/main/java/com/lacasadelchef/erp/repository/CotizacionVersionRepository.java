package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.CotizacionVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CotizacionVersionRepository extends JpaRepository<CotizacionVersion, Integer> {

    List<CotizacionVersion> findByCotizacionIdCotizacionOrderByNumeroVersionDesc(Integer idCotizacion);
}
