package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.PuestoEmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.PuestoEmpleadoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.PuestoEmpleado;
import com.lacasadelchef.erp.repository.PuestoEmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PuestoEmpleadoServiceImpl implements PuestoEmpleadoService {

    private static final String TABLA = "puesto_empleado";

    private final PuestoEmpleadoRepository puestoEmpleadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<PuestoEmpleadoResponse> listar() {
        return puestoEmpleadoRepository.findAll().stream().map(PuestoEmpleadoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PuestoEmpleadoResponse obtenerPorId(Integer id) {
        return PuestoEmpleadoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public PuestoEmpleadoResponse crear(PuestoEmpleadoRequest request) {
        PuestoEmpleado puestoEmpleado = new PuestoEmpleado();
        aplicar(request, puestoEmpleado);
        puestoEmpleado = puestoEmpleadoRepository.save(puestoEmpleado);
        bitacoraMovimientoService.registrar(TABLA, puestoEmpleado.getIdPuestoEmpleado(), Operacion.INSERT);
        return PuestoEmpleadoResponse.desde(puestoEmpleado);
    }

    @Override
    @Transactional
    public PuestoEmpleadoResponse actualizar(Integer id, PuestoEmpleadoRequest request) {
        PuestoEmpleado puestoEmpleado = buscar(id);
        aplicar(request, puestoEmpleado);
        puestoEmpleado = puestoEmpleadoRepository.save(puestoEmpleado);
        return PuestoEmpleadoResponse.desde(puestoEmpleado);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        PuestoEmpleado puestoEmpleado = buscar(id);
        puestoEmpleadoRepository.delete(puestoEmpleado);
        bitacoraMovimientoService.registrar(TABLA, puestoEmpleado.getIdPuestoEmpleado(), Operacion.DELETE);
    }

    private PuestoEmpleado buscar(Integer id) {
        return puestoEmpleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PuestoEmpleado", id));
    }

    private void aplicar(PuestoEmpleadoRequest request, PuestoEmpleado puestoEmpleado) {
        puestoEmpleado.setNombreRol(request.nombreRol().trim());
    }
}
