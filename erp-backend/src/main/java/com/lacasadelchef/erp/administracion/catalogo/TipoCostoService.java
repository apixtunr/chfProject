package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoCostoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoCostoResponse;

import java.util.List;

public interface TipoCostoService {

    List<TipoCostoResponse> listar();

    TipoCostoResponse obtenerPorId(Integer id);

    TipoCostoResponse crear(TipoCostoRequest request);

    TipoCostoResponse actualizar(Integer id, TipoCostoRequest request);

    void eliminar(Integer id);
}
