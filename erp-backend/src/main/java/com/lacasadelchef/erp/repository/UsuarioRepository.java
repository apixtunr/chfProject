package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @Query("""
            SELECT u FROM Usuario u
            JOIN FETCH u.rol
            JOIN FETCH u.estado
            LEFT JOIN FETCH u.empleado
            WHERE u.username = :username
            """)
    Optional<Usuario> findByUsername(@Param("username") String username);

    boolean existsByUsernameIgnoreCase(String username);

    Page<Usuario> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    /**
     * El usuario de un empleado, si lo tiene. Devuelve Optional y no una lista porque la
     * relacion es uno-a-uno: id_empleado tiene indice unico.
     *
     * La llave vive del lado del usuario y la entidad Empleado no apunta de vuelta, asi
     * que esta es la unica forma de preguntar "este empleado, ¿tiene acceso al sistema?".
     */
    Optional<Usuario> findByEmpleadoIdEmpleado(Integer idEmpleado);

    /** Los empleados de esta pagina que tienen usuario, para marcarlos en la lista. */
    @Query("SELECT u.empleado.idEmpleado FROM Usuario u WHERE u.empleado.idEmpleado IN :ids")
    java.util.List<Integer> idsEmpleadoConUsuario(@Param("ids") java.util.Collection<Integer> ids);
}
