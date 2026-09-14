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

    /** Confirma que el producto planificado se consumio de verdad: genera la salida de stock real. */
    EventoInventarioResponse confirmarConsumo(Integer idEvento, Integer idProducto);

    /**
     * Confirma de una vez todo lo planificado y aun no confirmado de un evento (se llama cuando
     * el evento pasa a EN CURSO en automatico). A diferencia de {@link #confirmarConsumo}, un
     * producto sin stock suficiente no interrumpe a los demas: se omite y queda registrado en el
     * log para revision manual, ya que esto corre sin un usuario presente para decidir que hacer.
     */
    void confirmarConsumoAutomatico(Integer idEvento);

    /**
     * Version disparada por el usuario desde el boton "Confirmar todo": a diferencia de
     * {@link #confirmarConsumoAutomatico}, reporta que se confirmo y que fallo (y por que)
     * para mostrarselo, en vez de solo dejarlo en el log.
     */
    EventoInventarioConfirmacionMasivaResponse confirmarTodo(Integer idEvento);

    /**
     * Corrige la cantidad de un producto YA confirmado (error humano al confirmar el consumo).
     * No es un simple update: genera un movimiento AJUSTE por la diferencia para que el stock
     * real quede sincronizado, sin borrar el movimiento SALIDA original del historial.
     */
    EventoInventarioResponse corregirConsumo(Integer idEvento, Integer idProducto, EventoInventarioCorreccionRequest request);
}
