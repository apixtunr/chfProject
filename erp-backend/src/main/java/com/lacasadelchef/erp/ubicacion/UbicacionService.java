package com.lacasadelchef.erp.ubicacion;

import com.lacasadelchef.erp.ubicacion.dto.UbicacionRequest;
import com.lacasadelchef.erp.ubicacion.dto.UbicacionResponse;

import java.util.List;

public interface UbicacionService {

    List<UbicacionResponse> listar();

    UbicacionResponse obtenerPorId(Integer id);

    UbicacionResponse crear(UbicacionRequest request);

    UbicacionResponse actualizar(Integer id, UbicacionRequest request);

    void eliminar(Integer id);
}
