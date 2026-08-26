package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.CotizacionVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CotizacionVersionRepository extends JpaRepository<CotizacionVersion, Integer> {

    List<CotizacionVersion> findByCotizacionIdCotizacionOrderByNumeroVersionDesc(Integer idCotizacion);

    /** Candidatas para crear un evento: aceptadas y que ningun evento haya tomado todavia. */
    @Query("""
            SELECT cv FROM CotizacionVersion cv
            WHERE cv.estado.nombre = 'ACEPTADA'
              AND NOT EXISTS (SELECT 1 FROM Evento e WHERE e.cotizacionVersion = cv)
            ORDER BY cv.fechaVersion DESC
            """)
    List<CotizacionVersion> buscarAceptadasSinEvento();
}
