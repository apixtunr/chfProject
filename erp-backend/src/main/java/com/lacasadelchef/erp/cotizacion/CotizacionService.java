package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.cotizacion.dto.CotizacionRequest;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionResponse;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface CotizacionService {

    Page<CotizacionResponse> listar(Integer idCliente, Pageable pageable);

    CotizacionResponse obtenerPorId(Integer id);

    CotizacionResponse crear(CotizacionRequest request);

    CotizacionResponse actualizar(Integer id, CotizacionRequest request);

    void eliminar(Integer id);

    /**
     * Crea una cotizacion "interna" ya ACEPTADA (sin presupuesto, sin pasar por
     * envio/negociacion) para servir de base al detalle de un evento directo.
     */
    CotizacionVersion crearAceptadaParaEventoDirecto(Integer idCliente, LocalDate fechaEvento);
}
