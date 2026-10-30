package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Opcion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OpcionRepository extends JpaRepository<Opcion, Integer> {

    @Query("""
            SELECT o FROM Opcion o
            JOIN FETCH o.menuVista mv
            JOIN FETCH mv.modulo m
            ORDER BY m.orden, mv.orden, o.ordenMenuVista
            """)
    List<Opcion> findAllEnOrdenDeMenu();
}
