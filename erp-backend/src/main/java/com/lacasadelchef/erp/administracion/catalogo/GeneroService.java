package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.GeneroRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.GeneroResponse;

import java.util.List;

public interface GeneroService {

    List<GeneroResponse> listar();

    GeneroResponse obtenerPorId(Integer id);

    GeneroResponse crear(GeneroRequest request);

    GeneroResponse actualizar(Integer id, GeneroRequest request);

    void eliminar(Integer id);
}
