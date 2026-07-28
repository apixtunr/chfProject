package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.inventario.dto.CategoriaProductoRequest;
import com.lacasadelchef.erp.inventario.dto.CategoriaProductoResponse;

import java.util.List;

public interface CategoriaProductoService {

    List<CategoriaProductoResponse> listar();

    CategoriaProductoResponse obtenerPorId(Integer id);

    CategoriaProductoResponse crear(CategoriaProductoRequest request);

    CategoriaProductoResponse actualizar(Integer id, CategoriaProductoRequest request);

    void eliminar(Integer id);
}
