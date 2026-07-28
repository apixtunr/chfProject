package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.PuestoEmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.PuestoEmpleadoResponse;

import java.util.List;

public interface PuestoEmpleadoService {

    List<PuestoEmpleadoResponse> listar();

    PuestoEmpleadoResponse obtenerPorId(Integer id);

    PuestoEmpleadoResponse crear(PuestoEmpleadoRequest request);

    PuestoEmpleadoResponse actualizar(Integer id, PuestoEmpleadoRequest request);

    void eliminar(Integer id);
}
