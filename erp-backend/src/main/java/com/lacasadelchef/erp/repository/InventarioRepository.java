package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Inventario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Integer> {

    Optional<Inventario> findByProductoIdProducto(Integer idProducto);

    /**
     * Obtiene el stock del producto y lo bloquea hasta que termine la transacción.
     *
     * @param idProducto producto a consultar.
     * @return inventario bloqueado o un resultado vacío si el producto no tiene stock registrado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventario i WHERE i.producto.idProducto = :idProducto")
    Optional<Inventario> bloquearPorProducto(@Param("idProducto") Integer idProducto);

    @Query("SELECT i FROM Inventario i WHERE i.cantidadMinima > 0 AND i.cantidadTotal <= i.cantidadMinima")
    List<Inventario> findBajoStockMinimo();
}
