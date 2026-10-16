package com.lacasadelchef.erp.administracion.vehiculo;

import java.util.List;

import com.lacasadelchef.erp.administracion.vehiculo.dto.MarcaVehiculoRequest;
import com.lacasadelchef.erp.administracion.vehiculo.dto.MarcaVehiculoResponse;

public interface MarcaVehiculoService {

    List<MarcaVehiculoResponse> listar();

    MarcaVehiculoResponse obtenerPorId(Integer id);

    MarcaVehiculoResponse crear(MarcaVehiculoRequest request);

    MarcaVehiculoResponse actualizar(Integer id, MarcaVehiculoRequest request);

    void eliminar(Integer id);
}
