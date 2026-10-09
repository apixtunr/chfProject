package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.VPagoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VPagoEventoRepository extends JpaRepository<VPagoEvento, Integer> {

    /**
     * filtro admite: PENDIENTE (le falta abonar) o PAGADO (ya sin saldo). Un evento cancelado
     * no figura como pendiente: lo que debe o se le devuelve quedo en el acuerdo de cancelacion.
     */
    @Query("""
            SELECT v FROM VPagoEvento v
            WHERE ((:filtro = 'PAGADO' AND v.pendiente <= 0)
                OR (:filtro = 'PENDIENTE' AND v.pendiente > 0 AND v.estadoNombre <> 'CANCELADO'))
              AND (CAST(:idCliente AS integer) IS NULL OR v.idCliente = :idCliente)
              AND (CAST(:fechaDesde AS date) IS NULL OR v.fechaEvento >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR v.fechaEvento <= :fechaHasta)
            """)
    Page<VPagoEvento> buscar(@Param("filtro") String filtro,
                              @Param("idCliente") Integer idCliente,
                              @Param("fechaDesde") LocalDate fechaDesde,
                              @Param("fechaHasta") LocalDate fechaHasta,
                              Pageable pageable);

    /** Cuentas por cobrar: los eventos con saldo, sin los cancelados, del mas proximo al mas lejano. */
    @Query("""
            SELECT v FROM VPagoEvento v
            WHERE v.pendiente > 0 AND v.estadoNombre <> 'CANCELADO'
              AND (CAST(:idCliente AS integer) IS NULL OR v.idCliente = :idCliente)
            ORDER BY v.fechaEvento, v.idEvento
            """)
    List<VPagoEvento> conSaldo(@Param("idCliente") Integer idCliente);
}
