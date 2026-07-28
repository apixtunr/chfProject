package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.MenuVistaRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.MenuVistaResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.MenuVista;
import com.lacasadelchef.erp.entity.Modulo;
import com.lacasadelchef.erp.repository.MenuVistaRepository;
import com.lacasadelchef.erp.repository.ModuloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuVistaServiceImpl implements MenuVistaService {

    private static final String TABLA = "menu_vista";

    private final MenuVistaRepository menuVistaRepository;
    private final ModuloRepository moduloRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<MenuVistaResponse> listar(Integer idModulo) {
        List<MenuVista> vistas = menuVistaRepository.findAll();
        return vistas.stream()
                .filter(v -> idModulo == null || v.getModulo().getIdModulo().equals(idModulo))
                .map(MenuVistaResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuVistaResponse obtenerPorId(Integer id) {
        return MenuVistaResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public MenuVistaResponse crear(MenuVistaRequest request) {
        MenuVista menuVista = new MenuVista();
        aplicar(request, menuVista);
        menuVista = menuVistaRepository.save(menuVista);
        bitacoraMovimientoService.registrar(TABLA, menuVista.getIdMenuVista(), Operacion.INSERT);
        return MenuVistaResponse.desde(menuVista);
    }

    @Override
    @Transactional
    public MenuVistaResponse actualizar(Integer id, MenuVistaRequest request) {
        MenuVista menuVista = buscar(id);
        aplicar(request, menuVista);
        menuVista = menuVistaRepository.save(menuVista);
        bitacoraMovimientoService.registrar(TABLA, menuVista.getIdMenuVista(), Operacion.UPDATE);
        return MenuVistaResponse.desde(menuVista);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        MenuVista menuVista = buscar(id);
        menuVistaRepository.delete(menuVista);
        bitacoraMovimientoService.registrar(TABLA, menuVista.getIdMenuVista(), Operacion.DELETE);
    }

    private MenuVista buscar(Integer id) {
        return menuVistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuVista", id));
    }

    private void aplicar(MenuVistaRequest request, MenuVista menuVista) {
        Modulo modulo = moduloRepository.findById(request.idModulo())
                .orElseThrow(() -> new ResourceNotFoundException("Modulo", request.idModulo()));
        menuVista.setModulo(modulo);
        menuVista.setNombre(request.nombre().trim());
        menuVista.setOrden(request.orden() == null ? 0 : request.orden());
    }
}
