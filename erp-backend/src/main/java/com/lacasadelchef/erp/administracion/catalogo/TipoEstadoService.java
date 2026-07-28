package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEstadoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEstadoResponse;

import java.util.List;

public interface TipoEstadoService {

    List<TipoEstadoResponse> listar();

    TipoEstadoResponse obtenerPorId(Integer id);

    TipoEstadoResponse crear(TipoEstadoRequest request);

    TipoEstadoResponse actualizar(Integer id, TipoEstadoRequest request);

    void eliminar(Integer id);
}
