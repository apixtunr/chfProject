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
}
