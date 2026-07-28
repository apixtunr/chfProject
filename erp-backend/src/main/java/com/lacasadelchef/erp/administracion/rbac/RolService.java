package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.RolRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.RolResponse;

import java.util.List;

public interface RolService {

    List<RolResponse> listar();

    RolResponse obtenerPorId(Integer id);

    RolResponse crear(RolRequest request);

    RolResponse actualizar(Integer id, RolRequest request);

    void eliminar(Integer id);
}
