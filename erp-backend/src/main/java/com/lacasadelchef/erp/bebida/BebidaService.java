package com.lacasadelchef.erp.bebida;

import com.lacasadelchef.erp.bebida.dto.BebidaRequest;
import com.lacasadelchef.erp.bebida.dto.BebidaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BebidaService {

    Page<BebidaResponse> listar(String nombre, Pageable pageable);

    /** Las activas, para asignarlas a un plato. */
    List<BebidaResponse> listarActivas();

    BebidaResponse obtenerPorId(Integer id);

    BebidaResponse crear(BebidaRequest request);

    BebidaResponse actualizar(Integer id, BebidaRequest request);

    void eliminar(Integer id);
}
