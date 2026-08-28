package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.MenuPlato;
import com.lacasadelchef.erp.entity.id.MenuPlatoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MenuPlatoRepository extends JpaRepository<MenuPlato, MenuPlatoId> {

    List<MenuPlato> findByMenuIdMenuOrderByOrdenMenu(Integer idMenu);

    /** Precio de un menu completo: suma de sus platos. 0 si todavia no tiene ninguno. */
    @Query("SELECT COALESCE(SUM(mp.precioUnitario), 0) FROM MenuPlato mp WHERE mp.menu.idMenu = :idMenu")
    BigDecimal sumarPrecioPorMenu(@Param("idMenu") Integer idMenu);
}
