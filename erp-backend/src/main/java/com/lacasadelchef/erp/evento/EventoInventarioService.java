package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.EventoInventarioRequest;
import com.lacasadelchef.erp.evento.dto.EventoInventarioResponse;

import java.util.List;

public interface EventoInventarioService {

    List<EventoInventarioResponse> listar(Integer idEvento);

    EventoInventarioResponse agregar(Integer idEvento, Integer idProducto, EventoInventarioRequest request);

    EventoInventarioResponse actualizar(Integer idEvento, Integer idProducto, EventoInventarioRequest request);

    void quitar(Integer idEvento, Integer idProducto);

    /** Confirma que el producto planificado se consumio de verdad: genera la salida de stock real. */
    EventoInventarioResponse confirmarConsumo(Integer idEvento, Integer idProducto);
}
