package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.pago.dto.MetodoPagoRequest;
import com.lacasadelchef.erp.pago.dto.MetodoPagoResponse;

import java.util.List;

public interface MetodoPagoService {

    List<MetodoPagoResponse> listar();

    MetodoPagoResponse obtenerPorId(Integer id);

    MetodoPagoResponse crear(MetodoPagoRequest request);

    MetodoPagoResponse actualizar(Integer id, MetodoPagoRequest request);

    void eliminar(Integer id);
}
