package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.BitacoraMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BitacoraMovimientoRepository extends JpaRepository<BitacoraMovimiento, Long> {

    @Query(value = """
            SELECT b FROM BitacoraMovimiento b
            LEFT JOIN FETCH b.usuario u
            LEFT JOIN FETCH u.empleado
            WHERE (CAST(:idUsuario AS integer) IS NULL OR b.usuario.idUsuario = :idUsuario)
              AND (:tabla IS NULL OR b.tablaAfectada = :tabla)
              AND (CAST(:fechaDesde AS date) IS NULL OR CAST(b.fechaMovimiento AS date) >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR CAST(b.fechaMovimiento AS date) <= :fechaHasta)
            """,
            countQuery = """
            SELECT COUNT(b) FROM BitacoraMovimiento b
            WHERE (CAST(:idUsuario AS integer) IS NULL OR b.usuario.idUsuario = :idUsuario)
              AND (:tabla IS NULL OR b.tablaAfectada = :tabla)
              AND (CAST(:fechaDesde AS date) IS NULL OR CAST(b.fechaMovimiento AS date) >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR CAST(b.fechaMovimiento AS date) <= :fechaHasta)
            """)
    Page<BitacoraMovimiento> buscar(@Param("idUsuario") Integer idUsuario,
                                     @Param("tabla") String tabla,
                                     @Param("fechaDesde") LocalDate fechaDesde,
                                     @Param("fechaHasta") LocalDate fechaHasta,
                                     Pageable pageable);

    /** Para poblar el filtro "Tabla" con las tablas que realmente tienen movimientos registrados. */
    @Query("SELECT DISTINCT b.tablaAfectada FROM BitacoraMovimiento b ORDER BY b.tablaAfectada")
    List<String> listarTablasDistintas();
}
