package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.OpcionRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.OpcionResponse;

import java.util.List;

public interface OpcionService {

    List<OpcionResponse> listar(Integer idMenuVista);

    OpcionResponse obtenerPorId(Integer id);

    OpcionResponse crear(OpcionRequest request);

    OpcionResponse actualizar(Integer id, OpcionRequest request);

    void eliminar(Integer id);
}
