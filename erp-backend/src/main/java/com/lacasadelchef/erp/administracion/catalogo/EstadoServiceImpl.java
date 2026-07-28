package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.EstadoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.EstadoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.TipoEstado;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.TipoEstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadoServiceImpl implements EstadoService {

    private static final String TABLA = "estado";

    private final EstadoRepository estadoRepository;
    private final TipoEstadoRepository tipoEstadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<EstadoResponse> listar(Integer idTipoEstado) {
        List<Estado> estados = estadoRepository.findAll();
        return estados.stream()
                .filter(e -> idTipoEstado == null || e.getTipoEstado().getIdTipoEstado().equals(idTipoEstado))
                .map(EstadoResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoResponse obtenerPorId(Integer id) {
        return EstadoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public EstadoResponse crear(EstadoRequest request) {
        Estado estado = new Estado();
        aplicar(request, estado);
        estado = estadoRepository.save(estado);
        bitacoraMovimientoService.registrar(TABLA, estado.getIdEstado(), Operacion.INSERT);
        return EstadoResponse.desde(estado);
    }

    @Override
    @Transactional
    public EstadoResponse actualizar(Integer id, EstadoRequest request) {
        Estado estado = buscar(id);
        aplicar(request, estado);
        estado = estadoRepository.save(estado);
        bitacoraMovimientoService.registrar(TABLA, estado.getIdEstado(), Operacion.UPDATE);
        return EstadoResponse.desde(estado);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Estado estado = buscar(id);
        estadoRepository.delete(estado);
        bitacoraMovimientoService.registrar(TABLA, estado.getIdEstado(), Operacion.DELETE);
    }

    private Estado buscar(Integer id) {
        return estadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estado", id));
    }

    private void aplicar(EstadoRequest request, Estado estado) {
        TipoEstado tipoEstado = tipoEstadoRepository.findById(request.idTipoEstado())
                .orElseThrow(() -> new ResourceNotFoundException("TipoEstado", request.idTipoEstado()));
        estado.setTipoEstado(tipoEstado);
        estado.setNombre(request.nombre().trim());
    }
}
