package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Bebida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BebidaRepository extends JpaRepository<Bebida, Integer> {

    Page<Bebida> findByNombreBebidaContainingIgnoreCase(String nombreBebida, Pageable pageable);

    List<Bebida> findByEstadoNombreOrderByNombreBebida(String estado);

    boolean existsByNombreBebidaIgnoreCase(String nombreBebida);

    boolean existsByNombreBebidaIgnoreCaseAndIdBebidaNot(String nombreBebida, Integer idBebida);

    /** Platos que la incluyen. */
    @Query("SELECT COUNT(pb) FROM PlatoBebida pb WHERE pb.bebida.idBebida = :idBebida")
    long contarPlatos(@Param("idBebida") Integer idBebida);

    /** Lineas de cotizacion o de evento que la eligieron. */
    @Query("""
            SELECT (SELECT COUNT(d) FROM DetalleCotizacion d WHERE d.bebida.idBebida = :idBebida)
                 + (SELECT COUNT(e) FROM DetalleEvento e WHERE e.bebida.idBebida = :idBebida)
            """)
    long contarLineas(@Param("idBebida") Integer idBebida);
}
