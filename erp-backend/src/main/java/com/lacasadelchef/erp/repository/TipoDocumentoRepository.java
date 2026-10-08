package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Integer> {

    Optional<TipoDocumento> findByNombreTipo(String nombreTipo);
}
