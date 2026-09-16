package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.inventario.dto.MovimientoInventarioRequest;
import com.lacasadelchef.erp.inventario.dto.MovimientoInventarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovimientoInventarioService {

    Page<MovimientoInventarioResponse> listar(Integer idProducto, Integer idEvento, Pageable pageable);

    MovimientoInventarioResponse registrar(MovimientoInventarioRequest request);
}
