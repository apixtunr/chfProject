package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.DocumentoEmpleado;
import com.lacasadelchef.erp.entity.id.DocumentoEmpleadoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoEmpleadoRepository extends JpaRepository<DocumentoEmpleado, DocumentoEmpleadoId> {

    List<DocumentoEmpleado> findByEmpleadoIdEmpleado(Integer idEmpleado);
}
