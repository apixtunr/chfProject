package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEventoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEventoResponse;

import java.util.List;

public interface TipoEventoService {

    List<TipoEventoResponse> listar();

    TipoEventoResponse obtenerPorId(Integer id);

    TipoEventoResponse crear(TipoEventoRequest request);

    TipoEventoResponse actualizar(Integer id, TipoEventoRequest request);

    void eliminar(Integer id);
}
