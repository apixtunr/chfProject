package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEstadoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEstadoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.TipoEstado;
import com.lacasadelchef.erp.repository.TipoEstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoEstadoServiceImpl implements TipoEstadoService {

    private static final String TABLA = "tipo_estado";

    private final TipoEstadoRepository tipoEstadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<TipoEstadoResponse> listar() {
        return tipoEstadoRepository.findAll().stream().map(TipoEstadoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoEstadoResponse obtenerPorId(Integer id) {
        return TipoEstadoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public TipoEstadoResponse crear(TipoEstadoRequest request) {
        TipoEstado tipoEstado = new TipoEstado();
        aplicar(request, tipoEstado);
        tipoEstado = tipoEstadoRepository.save(tipoEstado);
        bitacoraMovimientoService.registrar(TABLA, tipoEstado.getIdTipoEstado(), Operacion.INSERT);
        return TipoEstadoResponse.desde(tipoEstado);
    }

    @Override
    @Transactional
    public TipoEstadoResponse actualizar(Integer id, TipoEstadoRequest request) {
        TipoEstado tipoEstado = buscar(id);
        aplicar(request, tipoEstado);
        tipoEstado = tipoEstadoRepository.save(tipoEstado);
        bitacoraMovimientoService.registrar(TABLA, tipoEstado.getIdTipoEstado(), Operacion.UPDATE);
        return TipoEstadoResponse.desde(tipoEstado);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        TipoEstado tipoEstado = buscar(id);
        tipoEstadoRepository.delete(tipoEstado);
        bitacoraMovimientoService.registrar(TABLA, tipoEstado.getIdTipoEstado(), Operacion.DELETE);
    }

    private TipoEstado buscar(Integer id) {
        return tipoEstadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoEstado", id));
    }

    private void aplicar(TipoEstadoRequest request, TipoEstado tipoEstado) {
        tipoEstado.setNombreTipo(request.nombreTipo().trim());
    }
}
