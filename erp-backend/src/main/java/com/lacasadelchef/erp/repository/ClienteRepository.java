package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Page<Cliente> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    boolean existsByNitIgnoreCase(String nit);
}
