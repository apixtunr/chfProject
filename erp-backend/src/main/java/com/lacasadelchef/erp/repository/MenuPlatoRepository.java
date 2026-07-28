package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.MenuPlato;
import com.lacasadelchef.erp.entity.id.MenuPlatoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuPlatoRepository extends JpaRepository<MenuPlato, MenuPlatoId> {

    List<MenuPlato> findByMenuIdMenuOrderByOrdenMenu(Integer idMenu);
}
