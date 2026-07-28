package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.inventario.dto.InventarioMinimoRequest;
import com.lacasadelchef.erp.inventario.dto.InventarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventarioService {

    Page<InventarioResponse> listar(Pageable pageable);

    InventarioResponse obtenerPorProducto(Integer idProducto);

    /** Crea el registro de inventario si aun no existe (upsert de cantidad_minima). */
    InventarioResponse actualizarMinimo(Integer idProducto, InventarioMinimoRequest request);

    List<InventarioResponse> alertasBajoStock();
}
