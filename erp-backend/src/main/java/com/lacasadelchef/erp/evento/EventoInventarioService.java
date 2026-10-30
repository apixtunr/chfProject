package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.evento.dto.EventoInventarioConfirmacionMasivaResponse;
import com.lacasadelchef.erp.evento.dto.EventoInventarioCorreccionRequest;
import com.lacasadelchef.erp.evento.dto.EventoInventarioRequest;
import com.lacasadelchef.erp.evento.dto.EventoInventarioResponse;

import java.util.List;

public interface EventoInventarioService {

    List<EventoInventarioResponse> listar(Integer idEvento);

    EventoInventarioResponse agregar(Integer idEvento, Integer idProducto, EventoInventarioRequest request);

    EventoInventarioResponse actualizar(Integer idEvento, Integer idProducto, EventoInventarioRequest request);

    void quitar(Integer idEvento, Integer idProducto);

    EventoInventarioResponse confirmarConsumo(Integer idEvento, Integer idProducto);

    void confirmarConsumoAutomatico(Integer idEvento);

    EventoInventarioConfirmacionMasivaResponse confirmarTodo(Integer idEvento);

    EventoInventarioResponse corregirConsumo(Integer idEvento, Integer idProducto, EventoInventarioCorreccionRequest request);
}
