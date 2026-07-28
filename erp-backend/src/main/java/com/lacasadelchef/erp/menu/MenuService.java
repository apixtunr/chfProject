package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.menu.dto.MenuPlatoRequest;
import com.lacasadelchef.erp.menu.dto.MenuPlatoResponse;
import com.lacasadelchef.erp.menu.dto.MenuRequest;
import com.lacasadelchef.erp.menu.dto.MenuResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MenuService {

    Page<MenuResponse> listar(String nombre, Pageable pageable);

    MenuResponse obtenerPorId(Integer id);

    MenuResponse crear(MenuRequest request);

    MenuResponse actualizar(Integer id, MenuRequest request);

    void eliminar(Integer id);

    List<MenuPlatoResponse> listarPlatos(Integer idMenu);

    MenuPlatoResponse agregarPlato(Integer idMenu, Integer idPlato, MenuPlatoRequest request);

    MenuPlatoResponse actualizarPlato(Integer idMenu, Integer idPlato, MenuPlatoRequest request);

    void quitarPlato(Integer idMenu, Integer idPlato);
}
