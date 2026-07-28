package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.cotizacion.dto.DetalleCotizacionRequest;
import com.lacasadelchef.erp.cotizacion.dto.DetalleCotizacionResponse;

import java.util.List;

public interface DetalleCotizacionService {

    List<DetalleCotizacionResponse> listar(Integer idCotizacionVersion);

    DetalleCotizacionResponse agregar(Integer idCotizacionVersion, DetalleCotizacionRequest request);

    DetalleCotizacionResponse actualizar(Integer idCotizacionVersion, Integer idDetalle, DetalleCotizacionRequest request);

    void eliminar(Integer idCotizacionVersion, Integer idDetalle);
}
