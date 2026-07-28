package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.ModuloRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.ModuloResponse;

import java.util.List;

public interface ModuloService {

    List<ModuloResponse> listar();

    ModuloResponse obtenerPorId(Integer id);

    ModuloResponse crear(ModuloRequest request);

    ModuloResponse actualizar(Integer id, ModuloRequest request);

    void eliminar(Integer id);
}
