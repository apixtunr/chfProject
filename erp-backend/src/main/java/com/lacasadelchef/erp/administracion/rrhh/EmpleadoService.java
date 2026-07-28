package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmpleadoService {

    Page<EmpleadoResponse> listar(String nombre, Pageable pageable);

    EmpleadoResponse obtenerPorId(Integer id);

    EmpleadoResponse crear(EmpleadoRequest request);

    EmpleadoResponse actualizar(Integer id, EmpleadoRequest request);

    void eliminar(Integer id);
}
