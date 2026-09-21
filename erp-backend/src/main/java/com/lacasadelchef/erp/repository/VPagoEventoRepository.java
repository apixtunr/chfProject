package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.VPagoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface VPagoEventoRepository extends JpaRepository<VPagoEvento, Integer> {

    /** filtro admite: PENDIENTE (le falta abonar, sin importar el estado del evento) o PAGADO (ya sin saldo). */
    @Query("""
            SELECT v FROM VPagoEvento v
            WHERE ((:filtro = 'PAGADO' AND v.pendiente <= 0)
                OR (:filtro = 'PENDIENTE' AND v.pendiente > 0))
              AND (CAST(:idCliente AS integer) IS NULL OR v.idCliente = :idCliente)
              AND (CAST(:fechaDesde AS date) IS NULL OR v.fechaEvento >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR v.fechaEvento <= :fechaHasta)
            """)
    Page<VPagoEvento> buscar(@Param("filtro") String filtro,
                              @Param("idCliente") Integer idCliente,
                              @Param("fechaDesde") LocalDate fechaDesde,
                              @Param("fechaHasta") LocalDate fechaHasta,
                              Pageable pageable);
}
