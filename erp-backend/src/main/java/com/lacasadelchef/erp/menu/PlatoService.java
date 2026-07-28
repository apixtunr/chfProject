package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.menu.dto.PlatoRequest;
import com.lacasadelchef.erp.menu.dto.PlatoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlatoService {

    Page<PlatoResponse> listar(String nombre, Pageable pageable);

    PlatoResponse obtenerPorId(Integer id);

    PlatoResponse crear(PlatoRequest request);

    PlatoResponse actualizar(Integer id, PlatoRequest request);

    void eliminar(Integer id);
}
