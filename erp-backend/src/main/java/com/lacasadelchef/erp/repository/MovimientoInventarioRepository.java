package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.MovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    Page<MovimientoInventario> findByProductoIdProducto(Integer idProducto, Pageable pageable);

    Page<MovimientoInventario> findByEventoIdEvento(Integer idEvento, Pageable pageable);

    /** Reporte de movimientos de inventario de un periodo, del mas antiguo al mas reciente. */
    @Query("""
            SELECT m FROM MovimientoInventario m
            JOIN FETCH m.producto p
            LEFT JOIN FETCH p.categoria
            LEFT JOIN FETCH m.evento
            LEFT JOIN FETCH m.usuario
            WHERE (CAST(:desde AS timestamp) IS NULL OR m.fechaMovimiento >= :desde)
              AND (CAST(:hasta AS timestamp) IS NULL OR m.fechaMovimiento < :hasta)
              AND (CAST(:tipo AS string) IS NULL OR m.tipoMovimiento = :tipo)
              AND (CAST(:idProducto AS integer) IS NULL OR p.idProducto = :idProducto)
            ORDER BY m.fechaMovimiento, m.idMovimiento
            """)
    List<MovimientoInventario> reporteMovimientos(@Param("desde") LocalDateTime desde,
                                                  @Param("hasta") LocalDateTime hasta,
                                                  @Param("tipo") String tipo,
                                                  @Param("idProducto") Integer idProducto);
}
