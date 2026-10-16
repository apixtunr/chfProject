package com.lacasadelchef.erp.administracion.vehiculo;

import java.util.List;

import com.lacasadelchef.erp.administracion.vehiculo.dto.TipoPlacaRequest;
import com.lacasadelchef.erp.administracion.vehiculo.dto.TipoPlacaResponse;

public interface TipoPlacaService {

    List<TipoPlacaResponse> listar();

    TipoPlacaResponse obtenerPorId(Integer id);

    TipoPlacaResponse crear(TipoPlacaRequest request);

    TipoPlacaResponse actualizar(Integer id, TipoPlacaRequest request);

    void eliminar(Integer id);
}
