package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoDocumentoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoDocumentoResponse;

import java.util.List;

public interface TipoDocumentoService {

    List<TipoDocumentoResponse> listar();

    TipoDocumentoResponse obtenerPorId(Integer id);

    TipoDocumentoResponse crear(TipoDocumentoRequest request);

    TipoDocumentoResponse actualizar(Integer id, TipoDocumentoRequest request);

    void eliminar(Integer id);
}
