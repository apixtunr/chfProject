package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    Page<MovimientoInventario> findByProductoIdProducto(Integer idProducto, Pageable pageable);
}
