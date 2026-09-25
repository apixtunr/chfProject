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

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    /**
     * Busqueda del listado y de los selectores. "texto" busca en nombre, NIT, telefono y
     * correo; "digitos" es el mismo texto sin guiones ni espacios, para encontrar NIT y
     * telefonos escritos con otro formato. Cadena vacia = sin filtro (en texto, digitos y
     * estado); se evita null porque PostgreSQL no puede deducir el tipo de un parametro nulo.
     */
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

    /**
     * Candidatos a duplicado: mismo NIT (no CF), telefono, correo o nombre. Cadena vacia =
     * no comparar ese dato; excluir = id del cliente que se edita (0 si es nuevo).
     */
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

    /** Clientes con ese telefono (comparado sin guiones ni espacios), salvo el que se edita. */
    @Query("""
            SELECT c FROM Cliente c
            WHERE c.idCliente <> :excluir
              AND REPLACE(REPLACE(c.telefono, '-', ''), ' ', '') = :telefono
            """)
    List<Cliente> buscarPorTelefono(@Param("telefono") String telefono, @Param("excluir") Integer excluir);

    /** Clientes con ese correo (sin distinguir mayusculas), salvo el que se edita. */
    @Query("SELECT c FROM Cliente c WHERE c.idCliente <> :excluir AND LOWER(c.correo) = :correo")
    List<Cliente> buscarPorCorreo(@Param("correo") String correo, @Param("excluir") Integer excluir);

    // --- Negocio abierto: impide inactivar al cliente ---------------------------

    /** Cotizaciones cuya ultima version sigue en CREADA o ENVIADA. */
    @Query("""
            SELECT COUNT(cv) FROM CotizacionVersion cv
            WHERE cv.cotizacion.cliente.idCliente = :idCliente
              AND cv.estado.nombre IN ('CREADA', 'ENVIADA')
              AND cv.numeroVersion = (SELECT MAX(cv2.numeroVersion) FROM CotizacionVersion cv2
                                      WHERE cv2.cotizacion = cv.cotizacion)
            """)
    long contarCotizacionesAbiertas(@Param("idCliente") Integer idCliente);

    /** Eventos que todavia no terminan (el cliente sale de la cotizacion o del evento directo). */
    @Query("""
            SELECT COUNT(e) FROM Evento e
            LEFT JOIN e.cotizacionVersion cv
            LEFT JOIN cv.cotizacion c
            WHERE (c.cliente.idCliente = :idCliente OR e.cliente.idCliente = :idCliente)
              AND e.estado.nombre IN ('CREADO', 'PLANIFICADO', 'EN CURSO')
            """)
    long contarEventosVigentes(@Param("idCliente") Integer idCliente);

    /** Lo que el cliente todavia debe, sin contar eventos cancelados. */
    @Query("""
            SELECT COALESCE(SUM(v.pendiente), 0) FROM VPagoEvento v
            WHERE v.idCliente = :idCliente AND v.pendiente > 0 AND v.estadoNombre <> 'CANCELADO'
            """)
    BigDecimal saldoPendiente(@Param("idCliente") Integer idCliente);
}
