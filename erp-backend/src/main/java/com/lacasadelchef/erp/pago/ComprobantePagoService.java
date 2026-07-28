package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.pago.dto.ComprobantePagoRequest;
import com.lacasadelchef.erp.pago.dto.ComprobantePagoResponse;

import java.util.List;

public interface ComprobantePagoService {

    List<ComprobantePagoResponse> listar(Integer idPago);

    ComprobantePagoResponse agregar(Integer idPago, ComprobantePagoRequest request);

    ComprobantePagoResponse actualizar(Integer idPago, Integer idComprobante, ComprobantePagoRequest request);

    void eliminar(Integer idPago, Integer idComprobante);
}
