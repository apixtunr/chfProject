package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.EstadoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.EstadoResponse;

import java.util.List;

public interface EstadoService {

    List<EstadoResponse> listar(Integer idTipoEstado);

    EstadoResponse obtenerPorId(Integer id);

    EstadoResponse crear(EstadoRequest request);

    EstadoResponse actualizar(Integer id, EstadoRequest request);

    void eliminar(Integer id);
}
