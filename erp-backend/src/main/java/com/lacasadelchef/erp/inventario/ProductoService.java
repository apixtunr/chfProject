package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.inventario.dto.ProductoRequest;
import com.lacasadelchef.erp.inventario.dto.ProductoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductoService {

    Page<ProductoResponse> listar(String nombre, Pageable pageable);

    ProductoResponse obtenerPorId(Integer id);

    ProductoResponse crear(ProductoRequest request);

    ProductoResponse actualizar(Integer id, ProductoRequest request);

    void eliminar(Integer id);
}
