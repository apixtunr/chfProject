package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.ComprobantePago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprobantePagoRepository extends JpaRepository<ComprobantePago, Integer> {

    List<ComprobantePago> findByPagoIdPago(Integer idPago);
}
