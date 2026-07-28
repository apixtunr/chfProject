package com.lacasadelchef.erp.administracion.flota;

import com.lacasadelchef.erp.administracion.flota.dto.MarcaVehiculoRequest;
import com.lacasadelchef.erp.administracion.flota.dto.MarcaVehiculoResponse;

import java.util.List;

public interface MarcaVehiculoService {

    List<MarcaVehiculoResponse> listar();

    MarcaVehiculoResponse obtenerPorId(Integer id);

    MarcaVehiculoResponse crear(MarcaVehiculoRequest request);

    MarcaVehiculoResponse actualizar(Integer id, MarcaVehiculoRequest request);

    void eliminar(Integer id);
}
