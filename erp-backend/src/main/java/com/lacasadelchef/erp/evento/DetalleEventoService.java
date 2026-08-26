package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.DetalleEventoRequest;
import com.lacasadelchef.erp.evento.dto.DetalleEventoResponse;

import java.util.List;

public interface DetalleEventoService {

    List<DetalleEventoResponse> listar(Integer idEvento);

    DetalleEventoResponse agregar(Integer idEvento, DetalleEventoRequest request);

    DetalleEventoResponse actualizar(Integer idEvento, Integer idDetalle, DetalleEventoRequest request);

    void eliminar(Integer idEvento, Integer idDetalle);
}
