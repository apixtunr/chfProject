package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.ModuloRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.ModuloResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Modulo;
import com.lacasadelchef.erp.repository.ModuloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuloServiceImpl implements ModuloService {

    private static final String TABLA = "modulo";

    private final ModuloRepository moduloRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<ModuloResponse> listar() {
        return moduloRepository.findAll().stream().map(ModuloResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ModuloResponse obtenerPorId(Integer id) {
        return ModuloResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public ModuloResponse crear(ModuloRequest request) {
        Modulo modulo = new Modulo();
        aplicar(request, modulo);
        modulo = moduloRepository.save(modulo);
        bitacoraMovimientoService.registrar(TABLA, modulo.getIdModulo(), Operacion.INSERT);
        return ModuloResponse.desde(modulo);
    }

    @Override
    @Transactional
    public ModuloResponse actualizar(Integer id, ModuloRequest request) {
        Modulo modulo = buscar(id);
        aplicar(request, modulo);
        modulo = moduloRepository.save(modulo);
        return ModuloResponse.desde(modulo);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Modulo modulo = buscar(id);
        moduloRepository.delete(modulo);
        bitacoraMovimientoService.registrar(TABLA, modulo.getIdModulo(), Operacion.DELETE);
    }

    private Modulo buscar(Integer id) {
        return moduloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modulo", id));
    }

    private void aplicar(ModuloRequest request, Modulo modulo) {
        modulo.setNombre(request.nombre().trim());
        modulo.setOrden(request.orden() == null ? 0 : request.orden());
    }
}
