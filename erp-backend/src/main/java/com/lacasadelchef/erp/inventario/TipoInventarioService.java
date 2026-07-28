package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.inventario.dto.TipoInventarioRequest;
import com.lacasadelchef.erp.inventario.dto.TipoInventarioResponse;

import java.util.List;

public interface TipoInventarioService {

    List<TipoInventarioResponse> listar();

    TipoInventarioResponse obtenerPorId(Integer id);

    TipoInventarioResponse crear(TipoInventarioRequest request);

    TipoInventarioResponse actualizar(Integer id, TipoInventarioRequest request);

    void eliminar(Integer id);
}
