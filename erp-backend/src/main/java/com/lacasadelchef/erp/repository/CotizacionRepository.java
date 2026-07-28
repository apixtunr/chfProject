package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Cotizacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Integer> {

    Page<Cotizacion> findByClienteIdCliente(Integer idCliente, Pageable pageable);
}
