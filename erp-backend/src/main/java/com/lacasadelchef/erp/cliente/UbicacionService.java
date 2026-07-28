package com.lacasadelchef.erp.cliente;

import com.lacasadelchef.erp.cliente.dto.UbicacionRequest;
import com.lacasadelchef.erp.cliente.dto.UbicacionResponse;

import java.util.List;

public interface UbicacionService {

    List<UbicacionResponse> listar(Integer idCliente);

    UbicacionResponse obtenerPorId(Integer id);

    UbicacionResponse crear(UbicacionRequest request);

    UbicacionResponse actualizar(Integer id, UbicacionRequest request);

    void eliminar(Integer id);
}
