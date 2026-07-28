package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.RolOpcionRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.RolOpcionResponse;

import java.util.List;

public interface RolOpcionService {

    List<RolOpcionResponse> listar(Integer idRol);

    RolOpcionResponse asignar(Integer idRol, Integer idOpcion, RolOpcionRequest request);

    RolOpcionResponse actualizar(Integer idRol, Integer idOpcion, RolOpcionRequest request);

    void quitar(Integer idRol, Integer idOpcion);
}
