package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.cotizacion.dto.CotizacionVersionResponse;

import java.util.List;

public interface CotizacionVersionService {

    List<CotizacionVersionResponse> listarPorCotizacion(Integer idCotizacion);

    CotizacionVersionResponse obtenerPorId(Integer idCotizacionVersion);

    CotizacionVersionResponse crearVersion(Integer idCotizacion, boolean copiarUltimoDetalle);

    CotizacionVersionResponse cambiarEstado(Integer idCotizacionVersion, Integer idEstado);
}
