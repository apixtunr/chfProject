package com.lacasadelchef.erp.administracion.geografia;

import com.lacasadelchef.erp.administracion.geografia.dto.DepartamentoRequest;
import com.lacasadelchef.erp.administracion.geografia.dto.DepartamentoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Departamento;
import com.lacasadelchef.erp.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartamentoServiceImpl implements DepartamentoService {

    private static final String TABLA = "departamento";

    private final DepartamentoRepository departamentoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<DepartamentoResponse> listar() {
        return departamentoRepository.findAll().stream()
                .map(DepartamentoResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartamentoResponse obtenerPorId(Integer id) {
        return DepartamentoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public DepartamentoResponse crear(DepartamentoRequest request) {
        Departamento departamento = new Departamento();
        aplicar(request, departamento);
        departamento = departamentoRepository.save(departamento);
        bitacoraMovimientoService.registrar(TABLA, departamento.getIdDepartamento(), Operacion.INSERT);
        return DepartamentoResponse.desde(departamento);
    }

    @Override
    @Transactional
    public DepartamentoResponse actualizar(Integer id, DepartamentoRequest request) {
        Departamento departamento = buscar(id);
        aplicar(request, departamento);
        departamento = departamentoRepository.save(departamento);
        return DepartamentoResponse.desde(departamento);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Departamento departamento = buscar(id);
        departamentoRepository.delete(departamento);
        bitacoraMovimientoService.registrar(TABLA, departamento.getIdDepartamento(), Operacion.DELETE);
    }

    private Departamento buscar(Integer id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", id));
    }

    private void aplicar(DepartamentoRequest request, Departamento departamento) {
        departamento.setNombreDepartamento(request.nombreDepartamento().trim());
    }
}
