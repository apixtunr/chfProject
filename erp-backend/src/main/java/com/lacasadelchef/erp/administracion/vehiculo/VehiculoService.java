package com.lacasadelchef.erp.administracion.vehiculo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lacasadelchef.erp.administracion.vehiculo.dto.VehiculoRequest;
import com.lacasadelchef.erp.administracion.vehiculo.dto.VehiculoResponse;

public interface VehiculoService {

    Page<VehiculoResponse> listar(Pageable pageable);

    VehiculoResponse obtenerPorId(Integer id);

    VehiculoResponse crear(VehiculoRequest request);

    VehiculoResponse actualizar(Integer id, VehiculoRequest request);

    void eliminar(Integer id);
}
