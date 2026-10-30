package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Integer> {

    Optional<Inventario> findByProductoIdProducto(Integer idProducto);

    @Query("SELECT i FROM Inventario i WHERE i.cantidadMinima > 0 AND i.cantidadTotal <= i.cantidadMinima")
    List<Inventario> findBajoStockMinimo();
}
