package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.pago.dto.PagoRequest;
import com.lacasadelchef.erp.pago.dto.PagoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PagoService {

    Page<PagoResponse> listar(Integer idEvento, Pageable pageable);

    PagoResponse obtenerPorId(Integer id);

    PagoResponse crear(PagoRequest request);

    PagoResponse actualizar(Integer id, PagoRequest request);

    void eliminar(Integer id);

    PagoResponse cambiarEstado(Integer id, Integer idEstado);
}
