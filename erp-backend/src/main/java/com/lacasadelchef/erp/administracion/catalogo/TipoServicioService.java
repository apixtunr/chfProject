package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoServicioRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoServicioResponse;

import java.util.List;

public interface TipoServicioService {

    List<TipoServicioResponse> listar();

    TipoServicioResponse obtenerPorId(Integer id);

    TipoServicioResponse crear(TipoServicioRequest request);

    TipoServicioResponse actualizar(Integer id, TipoServicioRequest request);

    void eliminar(Integer id);
}
