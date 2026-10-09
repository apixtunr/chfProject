package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByEventoIdEvento(Integer idEvento);

    /** Si el cliente ya pago (y no se anulo) el reembolso de ese costo extra. */
    boolean existsByCostoEventoIdCostoEventoAndEstadoNombreNot(Integer idCostoEvento, String estado);

    Page<Pago> findByEventoIdEvento(Integer idEvento, Pageable pageable);

    /** Suma de abonos al precio del evento (sin contar reembolsos de costos ni pagos ANULADOS),
     * excluyendo opcionalmente un pago (para revalidar al editarlo sin contarse a si mismo). */
    @Query("""
            SELECT COALESCE(SUM(p.monto), 0) FROM Pago p
            WHERE p.evento.idEvento = :idEvento
              AND p.costoEvento IS NULL
              AND p.estado.nombre <> 'ANULADO'
              AND (CAST(:idPagoExcluir AS integer) IS NULL OR p.idPago <> :idPagoExcluir)
            """)
    BigDecimal sumarAbonado(@Param("idEvento") Integer idEvento, @Param("idPagoExcluir") Integer idPagoExcluir);

    /**
     * Lo abonado al precio del evento hasta ese pago inclusive (sin reembolsos ni anulados):
     * el saldo que se imprime en su recibo es el de ese momento, no el de hoy.
     */
    @Query("""
            SELECT COALESCE(SUM(p.monto), 0) FROM Pago p
            WHERE p.evento.idEvento = :idEvento
              AND p.costoEvento IS NULL
              AND p.estado.nombre <> 'ANULADO'
              AND p.idPago <= :idPago
            """)
    BigDecimal sumarAbonadoHasta(@Param("idEvento") Integer idEvento, @Param("idPago") Integer idPago);

    /**
     * Recibos emitidos: todos los pagos del periodo en el orden en que se registraron (el
     * numero de recibo es el del pago), incluidos los anulados para que no queden huecos en
     * la secuencia.
     */
    @Query("""
            SELECT p FROM Pago p
            JOIN FETCH p.evento e
            JOIN FETCH p.metodoPago
            JOIN FETCH p.estado
            LEFT JOIN FETCH p.costoEvento c
            LEFT JOIN FETCH c.tipoCosto
            LEFT JOIN FETCH p.usuario
            WHERE (CAST(:desde AS timestamp) IS NULL OR p.fechaPago >= :desde)
              AND (CAST(:hasta AS timestamp) IS NULL OR p.fechaPago < :hasta)
              AND (CAST(:idMetodoPago AS integer) IS NULL OR p.metodoPago.idMetodoPago = :idMetodoPago)
              AND (CAST(:idEstado AS integer) IS NULL OR p.estado.idEstado = :idEstado)
            ORDER BY p.idPago
            """)
    List<Pago> recibosEmitidos(@Param("desde") LocalDateTime desde,
                               @Param("hasta") LocalDateTime hasta,
                               @Param("idMetodoPago") Integer idMetodoPago,
                               @Param("idEstado") Integer idEstado);
}
