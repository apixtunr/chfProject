package com.lacasadelchef.erp.administracion.flota;

import com.lacasadelchef.erp.administracion.flota.dto.TipoPlacaRequest;
import com.lacasadelchef.erp.administracion.flota.dto.TipoPlacaResponse;

import java.util.List;

public interface TipoPlacaService {

    List<TipoPlacaResponse> listar();

    TipoPlacaResponse obtenerPorId(Integer id);

    TipoPlacaResponse crear(TipoPlacaRequest request);

    TipoPlacaResponse actualizar(Integer id, TipoPlacaRequest request);

    void eliminar(Integer id);
}
