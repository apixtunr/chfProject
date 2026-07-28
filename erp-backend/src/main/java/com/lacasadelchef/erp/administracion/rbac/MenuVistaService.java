package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.MenuVistaRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.MenuVistaResponse;

import java.util.List;

public interface MenuVistaService {

    List<MenuVistaResponse> listar(Integer idModulo);

    MenuVistaResponse obtenerPorId(Integer id);

    MenuVistaResponse crear(MenuVistaRequest request);

    MenuVistaResponse actualizar(Integer id, MenuVistaRequest request);

    void eliminar(Integer id);
}
