package com.lacasadelchef.erp.repository;

import com.lacasadelchef.erp.entity.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MunicipioRepository extends JpaRepository<Municipio, Integer> {

    List<Municipio> findByDepartamentoIdDepartamento(Integer idDepartamento);
}
