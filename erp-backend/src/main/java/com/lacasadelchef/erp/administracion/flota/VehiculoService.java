package com.lacasadelchef.erp.administracion.flota;

import com.lacasadelchef.erp.administracion.flota.dto.VehiculoRequest;
import com.lacasadelchef.erp.administracion.flota.dto.VehiculoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehiculoService {

    Page<VehiculoResponse> listar(Pageable pageable);

    VehiculoResponse obtenerPorId(Integer id);

    VehiculoResponse crear(VehiculoRequest request);

    VehiculoResponse actualizar(Integer id, VehiculoRequest request);

    void eliminar(Integer id);
}
