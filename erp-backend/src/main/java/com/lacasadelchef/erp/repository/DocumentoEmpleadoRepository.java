package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.DocumentoEmpleado;
import com.lacasadelchef.erp.entity.id.DocumentoEmpleadoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DocumentoEmpleadoRepository extends JpaRepository<DocumentoEmpleado, DocumentoEmpleadoId> {

    List<DocumentoEmpleado> findByEmpleadoIdEmpleado(Integer idEmpleado);

    /** Los documentos de un tipo para varios empleados a la vez (el DPI de una pagina del listado). */
    List<DocumentoEmpleado> findByEmpleadoIdEmpleadoInAndTipoDocumentoNombreTipo(Collection<Integer> idsEmpleado,
                                                                                 String nombreTipo);

    /** Quien tiene ya ese numero en ese tipo de documento (a lo sumo uno, por el indice unico de V31). */
    Optional<DocumentoEmpleado> findByTipoDocumentoIdTipoDocumentoAndNumeroDocumento(Integer idTipoDocumento,
                                                                                     String numeroDocumento);

    boolean existsByEmpleadoIdEmpleadoAndTipoDocumentoNombreTipo(Integer idEmpleado, String nombreTipo);

    /** De estos empleados, cuales tienen registrado un documento de ese tipo. */
    @Query("""
            SELECT d.empleado.idEmpleado FROM DocumentoEmpleado d
            WHERE d.empleado.idEmpleado IN :idsEmpleado
              AND d.tipoDocumento.nombreTipo = :nombreTipo
            """)
    List<Integer> idsEmpleadoConDocumento(@Param("idsEmpleado") Collection<Integer> idsEmpleado,
                                          @Param("nombreTipo") String nombreTipo);
}
