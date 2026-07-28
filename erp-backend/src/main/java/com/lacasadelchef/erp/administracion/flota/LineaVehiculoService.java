package com.lacasadelchef.erp.administracion.flota;

import com.lacasadelchef.erp.administracion.flota.dto.LineaVehiculoRequest;
import com.lacasadelchef.erp.administracion.flota.dto.LineaVehiculoResponse;

import java.util.List;

public interface LineaVehiculoService {

    List<LineaVehiculoResponse> listar(Integer idMarcaVehiculo);

    LineaVehiculoResponse obtenerPorId(Integer id);

    LineaVehiculoResponse crear(LineaVehiculoRequest request);

    LineaVehiculoResponse actualizar(Integer id, LineaVehiculoRequest request);

    void eliminar(Integer id);
}
