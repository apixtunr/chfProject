package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.RolOpcion;
import com.lacasadelchef.erp.entity.id.RolOpcionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RolOpcionRepository extends JpaRepository<RolOpcion, RolOpcionId> {

    @Query("""
            SELECT ro FROM RolOpcion ro
            JOIN FETCH ro.opcion o
            JOIN FETCH o.menuVista mv
            JOIN FETCH mv.modulo m
            WHERE ro.rol.idRol = :idRol
            ORDER BY m.orden, mv.orden, o.ordenMenuVista
            """)
    List<RolOpcion> findPermisosByRol(@Param("idRol") Integer idRol);

    Optional<RolOpcion> findByRolIdRolAndOpcionPaginaUrl(Integer idRol, String paginaUrl);
}
