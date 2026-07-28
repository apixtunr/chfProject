package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Integer> {

    Page<Menu> findByNombreMenuContainingIgnoreCase(String nombreMenu, Pageable pageable);
}
