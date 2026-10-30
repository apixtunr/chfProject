package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    @Query("""
            SELECT c FROM Cliente c
            WHERE (:estado = '' OR c.estado.nombre = :estado)
              AND (:texto = ''
                   OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(c.correo) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR (:digitos <> '' AND REPLACE(c.nit, '-', '') LIKE CONCAT('%', :digitos, '%'))
                   OR (:digitos <> '' AND REPLACE(REPLACE(c.telefono, '-', ''), ' ', '') LIKE CONCAT('%', :digitos, '%')))
            """)
    Page<Cliente> buscar(@Param("texto") String texto,
                         @Param("digitos") String digitos,
                         @Param("estado") String estado,
                         Pageable pageable);

    Optional<Cliente> findByNit(String nit);

    @Query("""
            SELECT c FROM Cliente c
            WHERE c.idCliente <> :excluir
              AND ((:nit <> '' AND c.nit = :nit)
                OR (:telefono <> '' AND REPLACE(REPLACE(c.telefono, '-', ''), ' ', '') = :telefono)
                OR (:correo <> '' AND LOWER(c.correo) = :correo)
                OR (:nombre <> '' AND LOWER(c.nombre) = :nombre))
            ORDER BY c.idCliente
            """)
    List<Cliente> buscarParecidos(@Param("nit") String nit,
                                  @Param("telefono") String telefono,
                                  @Param("correo") String correo,
                                  @Param("nombre") String nombre,
                                  @Param("excluir") Integer excluir);

    @Query("""
            SELECT COUNT(cv) FROM CotizacionVersion cv
            WHERE cv.cotizacion.cliente.idCliente = :idCliente
              AND cv.estado.nombre IN ('CREADA', 'ENVIADA')
              AND cv.numeroVersion = (SELECT MAX(cv2.numeroVersion) FROM CotizacionVersion cv2
                                      WHERE cv2.cotizacion = cv.cotizacion)
            """)
    long contarCotizacionesAbiertas(@Param("idCliente") Integer idCliente);

    @Query("""
            SELECT COUNT(e) FROM Evento e
            LEFT JOIN e.cotizacionVersion cv
            LEFT JOIN cv.cotizacion c
            WHERE (c.cliente.idCliente = :idCliente OR e.cliente.idCliente = :idCliente)
              AND e.estado.nombre IN ('CREADO', 'PLANIFICADO', 'EN CURSO')
            """)
    long contarEventosVigentes(@Param("idCliente") Integer idCliente);

    @Query("""
            SELECT COALESCE(SUM(v.pendiente), 0) FROM VPagoEvento v
            WHERE v.idCliente = :idCliente AND v.pendiente > 0 AND v.estadoNombre <> 'CANCELADO'
            """)
    BigDecimal saldoPendiente(@Param("idCliente") Integer idCliente);

    @Query(nativeQuery = true, value = """
            SELECT c.id_cliente AS "idCliente",
                   c.nombre AS "nombre",
                   c.nit AS "nit",
                   c.telefono AS "telefono",
                   c.correo AS "correo",
                   m.nombre_municipio AS "municipio",
                   d.nombre_departamento AS "departamento",
                   es.nombre AS "estado",
                   c.fecha_creacion AS "fechaRegistro"
            FROM cliente c
            LEFT JOIN estado es ON es.id_estado = c.id_estado
            LEFT JOIN municipio m ON m.id_municipio = c.id_municipio
            LEFT JOIN departamento d ON d.id_departamento = m.id_departamento
            WHERE (CAST(:fechaDesde AS date) IS NULL OR CAST(c.fecha_creacion AS date) >= :fechaDesde)
              AND (CAST(:fechaHasta AS date) IS NULL OR CAST(c.fecha_creacion AS date) <= :fechaHasta)
              AND (CAST(:idEstado AS integer) IS NULL OR c.id_estado = :idEstado)
            ORDER BY c.fecha_creacion, c.id_cliente
            """)
    List<ClienteReporteFila> reporteClientes(@Param("fechaDesde") LocalDate fechaDesde,
                                             @Param("fechaHasta") LocalDate fechaHasta,
                                             @Param("idEstado") Integer idEstado);

    interface ClienteReporteFila {
        Integer getIdCliente();
        String getNombre();
        String getNit();
        String getTelefono();
        String getCorreo();
        String getMunicipio();
        String getDepartamento();
        String getEstado();
        LocalDateTime getFechaRegistro();
    }
}
