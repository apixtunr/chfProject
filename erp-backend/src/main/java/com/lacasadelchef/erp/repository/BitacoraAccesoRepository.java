package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.BitacoraAcceso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface BitacoraAccesoRepository extends JpaRepository<BitacoraAcceso, Long> {

    @Query("""
            SELECT b FROM BitacoraAcceso b
            WHERE (CAST(:idUsuario AS integer) IS NULL OR b.usuario.idUsuario = :idUsuario)
              AND (:resultado IS NULL OR b.resultado = :resultado)
              AND (CAST(:fechaDesde AS date) IS NULL OR CAST(b.fechaAcceso AS date) >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR CAST(b.fechaAcceso AS date) <= :fechaHasta)
            """)
    Page<BitacoraAcceso> buscar(@Param("idUsuario") Integer idUsuario,
                                 @Param("resultado") String resultado,
                                 @Param("fechaDesde") LocalDate fechaDesde,
                                 @Param("fechaHasta") LocalDate fechaHasta,
                                 Pageable pageable);
}
