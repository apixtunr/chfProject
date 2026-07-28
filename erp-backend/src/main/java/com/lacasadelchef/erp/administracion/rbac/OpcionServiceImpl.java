package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.OpcionRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.OpcionResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.MenuVista;
import com.lacasadelchef.erp.entity.Opcion;
import com.lacasadelchef.erp.repository.MenuVistaRepository;
import com.lacasadelchef.erp.repository.OpcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpcionServiceImpl implements OpcionService {

    private static final String TABLA = "opcion";

    private final OpcionRepository opcionRepository;
    private final MenuVistaRepository menuVistaRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<OpcionResponse> listar(Integer idMenuVista) {
        List<Opcion> opciones = opcionRepository.findAll();
        return opciones.stream()
                .filter(o -> idMenuVista == null || o.getMenuVista().getIdMenuVista().equals(idMenuVista))
                .map(OpcionResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OpcionResponse obtenerPorId(Integer id) {
        return OpcionResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public OpcionResponse crear(OpcionRequest request) {
        Opcion opcion = new Opcion();
        aplicar(request, opcion);
        opcion = opcionRepository.save(opcion);
        bitacoraMovimientoService.registrar(TABLA, opcion.getIdOpcion(), Operacion.INSERT);
        return OpcionResponse.desde(opcion);
    }

    @Override
    @Transactional
    public OpcionResponse actualizar(Integer id, OpcionRequest request) {
        Opcion opcion = buscar(id);
        aplicar(request, opcion);
        opcion = opcionRepository.save(opcion);
        bitacoraMovimientoService.registrar(TABLA, opcion.getIdOpcion(), Operacion.UPDATE);
        return OpcionResponse.desde(opcion);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Opcion opcion = buscar(id);
        opcionRepository.delete(opcion);
        bitacoraMovimientoService.registrar(TABLA, opcion.getIdOpcion(), Operacion.DELETE);
    }

    private Opcion buscar(Integer id) {
        return opcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opcion", id));
    }

    private void aplicar(OpcionRequest request, Opcion opcion) {
        MenuVista menuVista = menuVistaRepository.findById(request.idMenuVista())
                .orElseThrow(() -> new ResourceNotFoundException("MenuVista", request.idMenuVista()));
        opcion.setMenuVista(menuVista);
        opcion.setNombreOpcion(request.nombreOpcion().trim());
        opcion.setOrdenMenuVista(request.ordenMenuVista() == null ? 0 : request.ordenMenuVista());
        opcion.setPaginaUrl(request.paginaUrl());
        opcion.setAccion(request.accion());
    }
}
