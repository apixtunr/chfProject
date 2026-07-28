package com.lacasadelchef.erp.administracion.geografia;

import com.lacasadelchef.erp.administracion.geografia.dto.MunicipioRequest;
import com.lacasadelchef.erp.administracion.geografia.dto.MunicipioResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Departamento;
import com.lacasadelchef.erp.entity.Municipio;
import com.lacasadelchef.erp.repository.DepartamentoRepository;
import com.lacasadelchef.erp.repository.MunicipioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MunicipioServiceImpl implements MunicipioService {

    private static final String TABLA = "municipio";

    private final MunicipioRepository municipioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<MunicipioResponse> listar(Integer idDepartamento) {
        List<Municipio> municipios = (idDepartamento == null)
                ? municipioRepository.findAll()
                : municipioRepository.findByDepartamentoIdDepartamento(idDepartamento);
        return municipios.stream().map(MunicipioResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MunicipioResponse obtenerPorId(Integer id) {
        return MunicipioResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public MunicipioResponse crear(MunicipioRequest request) {
        Municipio municipio = new Municipio();
        aplicar(request, municipio);
        municipio = municipioRepository.save(municipio);
        bitacoraMovimientoService.registrar(TABLA, municipio.getIdMunicipio(), Operacion.INSERT);
        return MunicipioResponse.desde(municipio);
    }

    @Override
    @Transactional
    public MunicipioResponse actualizar(Integer id, MunicipioRequest request) {
        Municipio municipio = buscar(id);
        aplicar(request, municipio);
        municipio = municipioRepository.save(municipio);
        bitacoraMovimientoService.registrar(TABLA, municipio.getIdMunicipio(), Operacion.UPDATE);
        return MunicipioResponse.desde(municipio);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Municipio municipio = buscar(id);
        municipioRepository.delete(municipio);
        bitacoraMovimientoService.registrar(TABLA, municipio.getIdMunicipio(), Operacion.DELETE);
    }

    private Municipio buscar(Integer id) {
        return municipioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Municipio", id));
    }

    private void aplicar(MunicipioRequest request, Municipio municipio) {
        Departamento departamento = departamentoRepository.findById(request.idDepartamento())
                .orElseThrow(() -> new ResourceNotFoundException("Departamento", request.idDepartamento()));
        municipio.setDepartamento(departamento);
        municipio.setNombreMunicipio(request.nombreMunicipio().trim());
    }
}
