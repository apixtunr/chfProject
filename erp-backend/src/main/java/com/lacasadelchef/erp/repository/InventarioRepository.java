package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Integer> {

    Optional<Inventario> findByProductoIdProducto(Integer idProducto);

    /**
     * Productos que ya llegaron a su minimo (alertas de inventario). Se avisa al llegar,
     * no despues: con minimo 10 y 10 disponibles ya toca reabastecer. Los productos sin
     * minimo configurado (0) quedan fuera, porque si nadie definio cuanto es "poco" no
     * hay nada que avisar; la misma regla aplica el frontend en la pantalla de Stock.
     */
    @Query("SELECT i FROM Inventario i WHERE i.cantidadMinima > 0 AND i.cantidadTotal <= i.cantidadMinima")
    List<Inventario> findBajoStockMinimo();
}
