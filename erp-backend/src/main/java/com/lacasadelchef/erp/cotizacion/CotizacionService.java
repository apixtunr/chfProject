package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.cotizacion.dto.CotizacionRequest;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CotizacionService {

    Page<CotizacionResponse> listar(Integer idCliente, Pageable pageable);

    CotizacionResponse obtenerPorId(Integer id);

    CotizacionResponse crear(CotizacionRequest request);

    CotizacionResponse actualizar(Integer id, CotizacionRequest request);

    void eliminar(Integer id);
}
