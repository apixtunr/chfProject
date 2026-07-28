package com.lacasadelchef.erp.administracion.geografia;

import com.lacasadelchef.erp.administracion.geografia.dto.DepartamentoRequest;
import com.lacasadelchef.erp.administracion.geografia.dto.DepartamentoResponse;

import java.util.List;

public interface DepartamentoService {

    List<DepartamentoResponse> listar();

    DepartamentoResponse obtenerPorId(Integer id);

    DepartamentoResponse crear(DepartamentoRequest request);

    DepartamentoResponse actualizar(Integer id, DepartamentoRequest request);

    void eliminar(Integer id);
}
