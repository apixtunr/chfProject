package com.lacasadelchef.erp.administracion.vehiculo;

import java.util.List;

import com.lacasadelchef.erp.administracion.vehiculo.dto.LineaVehiculoRequest;
import com.lacasadelchef.erp.administracion.vehiculo.dto.LineaVehiculoResponse;

public interface LineaVehiculoService {

    List<LineaVehiculoResponse> listar(Integer idMarcaVehiculo);

    LineaVehiculoResponse obtenerPorId(Integer id);

    LineaVehiculoResponse crear(LineaVehiculoRequest request);

    LineaVehiculoResponse actualizar(Integer id, LineaVehiculoRequest request);

    void eliminar(Integer id);
}
