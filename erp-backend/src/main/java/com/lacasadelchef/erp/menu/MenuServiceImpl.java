package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Menu;
import com.lacasadelchef.erp.entity.MenuPlato;
import com.lacasadelchef.erp.entity.Plato;
import com.lacasadelchef.erp.entity.id.MenuPlatoId;
import com.lacasadelchef.erp.menu.dto.MenuPlatoRequest;
import com.lacasadelchef.erp.menu.dto.MenuPlatoResponse;
import com.lacasadelchef.erp.menu.dto.MenuRequest;
import com.lacasadelchef.erp.menu.dto.MenuResponse;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.MenuPlatoRepository;
import com.lacasadelchef.erp.repository.MenuRepository;
import com.lacasadelchef.erp.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private static final String TABLA_MENU = "menu";
    private static final String TABLA_MENU_PLATO = "menu_plato";

    private final MenuRepository menuRepository;
    private final PlatoRepository platoRepository;
    private final MenuPlatoRepository menuPlatoRepository;
    private final EstadoRepository estadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public Page<MenuResponse> listar(String nombre, Pageable pageable) {
        Page<Menu> page = (nombre == null || nombre.isBlank())
                ? menuRepository.findAll(pageable)
                : menuRepository.findByNombreMenuContainingIgnoreCase(nombre.trim(), pageable);
        return page.map(menu -> MenuResponse.desde(menu, menuPlatoRepository.sumarPrecioPorMenu(menu.getIdMenu())));
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse obtenerPorId(Integer id) {
        Menu menu = buscarMenu(id);
        return MenuResponse.desde(menu, menuPlatoRepository.sumarPrecioPorMenu(id));
    }

    @Override
    @Transactional
    public MenuResponse crear(MenuRequest request) {
        Menu menu = new Menu();
        aplicar(request, menu);
        menu = menuRepository.save(menu);
        bitacoraMovimientoService.registrar(TABLA_MENU, menu.getIdMenu(), Operacion.INSERT);
        return MenuResponse.desde(menu, menuPlatoRepository.sumarPrecioPorMenu(menu.getIdMenu()));
    }

    @Override
    @Transactional
    public MenuResponse actualizar(Integer id, MenuRequest request) {
        Menu menu = buscarMenu(id);
        aplicar(request, menu);
        menu = menuRepository.save(menu);
        bitacoraMovimientoService.registrar(TABLA_MENU, menu.getIdMenu(), Operacion.UPDATE);
        return MenuResponse.desde(menu, menuPlatoRepository.sumarPrecioPorMenu(id));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el menu esta referenciado en algun detalle de cotizacion, la FK lo impide
        // y el GlobalExceptionHandler lo traduce a HTTP 409.
        Menu menu = buscarMenu(id);
        menuRepository.delete(menu);
        bitacoraMovimientoService.registrar(TABLA_MENU, menu.getIdMenu(), Operacion.DELETE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuPlatoResponse> listarPlatos(Integer idMenu) {
        buscarMenu(idMenu);
        return menuPlatoRepository.findByMenuIdMenuOrderByOrdenMenu(idMenu).stream()
                .map(MenuPlatoResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public MenuPlatoResponse agregarPlato(Integer idMenu, Integer idPlato, MenuPlatoRequest request) {
        Menu menu = buscarMenu(idMenu);
        Plato plato = buscarPlato(idPlato);
        MenuPlato menuPlato = new MenuPlato();
        menuPlato.setId(new MenuPlatoId(idMenu, idPlato));
        menuPlato.setMenu(menu);
        menuPlato.setPlato(plato);
        aplicar(request, menuPlato);
        menuPlato = menuPlatoRepository.save(menuPlato);
        bitacoraMovimientoService.registrar(TABLA_MENU_PLATO, idMenu + "-" + idPlato, Operacion.INSERT);
        return MenuPlatoResponse.desde(menuPlato);
    }

    @Override
    @Transactional
    public MenuPlatoResponse actualizarPlato(Integer idMenu, Integer idPlato, MenuPlatoRequest request) {
        MenuPlato menuPlato = buscarMenuPlato(idMenu, idPlato);
        aplicar(request, menuPlato);
        menuPlato = menuPlatoRepository.save(menuPlato);
        bitacoraMovimientoService.registrar(TABLA_MENU_PLATO, idMenu + "-" + idPlato, Operacion.UPDATE);
        return MenuPlatoResponse.desde(menuPlato);
    }

    @Override
    @Transactional
    public void quitarPlato(Integer idMenu, Integer idPlato) {
        MenuPlato menuPlato = buscarMenuPlato(idMenu, idPlato);
        menuPlatoRepository.delete(menuPlato);
        bitacoraMovimientoService.registrar(TABLA_MENU_PLATO, idMenu + "-" + idPlato, Operacion.DELETE);
    }

    private Menu buscarMenu(Integer id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", id));
    }

    private Plato buscarPlato(Integer id) {
        return platoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plato", id));
    }

    private MenuPlato buscarMenuPlato(Integer idMenu, Integer idPlato) {
        return menuPlatoRepository.findById(new MenuPlatoId(idMenu, idPlato))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El plato %d no esta asociado al menu %d".formatted(idPlato, idMenu)));
    }

    private void aplicar(MenuRequest request, Menu menu) {
        Estado estado = estadoRepository.findById(request.idEstado())
                .orElseThrow(() -> new ResourceNotFoundException("Estado", request.idEstado()));
        menu.setNombreMenu(request.nombreMenu().trim());
        menu.setEstado(estado);
    }

    private void aplicar(MenuPlatoRequest request, MenuPlato menuPlato) {
        menuPlato.setPrecioUnitario(request.precioUnitario());
        menuPlato.setOrdenMenu(request.ordenMenu() == null ? 0 : request.ordenMenu());
    }
}
