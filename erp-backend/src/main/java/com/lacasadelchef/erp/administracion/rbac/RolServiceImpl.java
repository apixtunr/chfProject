package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.RolRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.RolResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private static final String TABLA = "rol";

    private final RolRepository rolRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<RolResponse> listar() {
        return rolRepository.findAll().stream().map(RolResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RolResponse obtenerPorId(Integer id) {
        return RolResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public RolResponse crear(RolRequest request) {
        Rol rol = new Rol();
        aplicar(request, rol);
        rol = rolRepository.save(rol);
        bitacoraMovimientoService.registrar(TABLA, rol.getIdRol(), Operacion.INSERT);
        return RolResponse.desde(rol);
    }

    @Override
    @Transactional
    public RolResponse actualizar(Integer id, RolRequest request) {
        Rol rol = buscar(id);
        aplicar(request, rol);
        rol = rolRepository.save(rol);
        bitacoraMovimientoService.registrar(TABLA, rol.getIdRol(), Operacion.UPDATE);
        return RolResponse.desde(rol);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        // Si el rol tiene usuarios o permisos asignados, la FK lo impide y el
        // GlobalExceptionHandler lo traduce a HTTP 409.
        Rol rol = buscar(id);
        rolRepository.delete(rol);
        bitacoraMovimientoService.registrar(TABLA, rol.getIdRol(), Operacion.DELETE);
    }

    private Rol buscar(Integer id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", id));
    }

    private void aplicar(RolRequest request, Rol rol) {
        rol.setNombreRol(request.nombreRol().trim());
    }
}
