package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.cotizacion.dto.ServicioCotizacionRequest;
import com.lacasadelchef.erp.cotizacion.dto.ServicioCotizacionResponse;

import java.util.List;

public interface ServicioCotizacionService {

    List<ServicioCotizacionResponse> listar(Integer idCotizacionVersion);

    ServicioCotizacionResponse agregar(Integer idCotizacionVersion, ServicioCotizacionRequest request);

    ServicioCotizacionResponse actualizar(Integer idCotizacionVersion, Integer idServicio, ServicioCotizacionRequest request);

    void eliminar(Integer idCotizacionVersion, Integer idServicio);
}
