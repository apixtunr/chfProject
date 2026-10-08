package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Empleado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    /** Busca por nombre, apellido o numero de DPI (el DPI se guarda sin espacios). */
    @Query("""
            SELECT e FROM Empleado e
            WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR EXISTS (SELECT d FROM DocumentoEmpleado d
                          WHERE d.empleado = e
                            AND d.tipoDocumento.nombreTipo = :tipoDpi
                            AND d.numeroDocumento LIKE CONCAT('%', :textoSinEspacios, '%'))
            """)
    Page<Empleado> buscar(@Param("texto") String texto,
                          @Param("textoSinEspacios") String textoSinEspacios,
                          @Param("tipoDpi") String tipoDpi,
                          Pageable pageable);
}
