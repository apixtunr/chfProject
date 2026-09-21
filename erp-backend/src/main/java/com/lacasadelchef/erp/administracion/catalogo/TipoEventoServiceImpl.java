package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEventoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEventoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.TipoEvento;
import com.lacasadelchef.erp.repository.TipoEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoEventoServiceImpl implements TipoEventoService {

    private static final String TABLA = "tipo_evento";

    private final TipoEventoRepository tipoEventoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<TipoEventoResponse> listar() {
        return tipoEventoRepository.findAll().stream().map(TipoEventoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoEventoResponse obtenerPorId(Integer id) {
        return TipoEventoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public TipoEventoResponse crear(TipoEventoRequest request) {
        TipoEvento tipoEvento = new TipoEvento();
        aplicar(request, tipoEvento);
        tipoEvento = tipoEventoRepository.save(tipoEvento);
        bitacoraMovimientoService.registrar(TABLA, tipoEvento.getIdTipoEvento(), Operacion.INSERT);
        return TipoEventoResponse.desde(tipoEvento);
    }

    @Override
    @Transactional
    public TipoEventoResponse actualizar(Integer id, TipoEventoRequest request) {
        TipoEvento tipoEvento = buscar(id);
        aplicar(request, tipoEvento);
        tipoEvento = tipoEventoRepository.save(tipoEvento);
        return TipoEventoResponse.desde(tipoEvento);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        TipoEvento tipoEvento = buscar(id);
        tipoEventoRepository.delete(tipoEvento);
        bitacoraMovimientoService.registrar(TABLA, tipoEvento.getIdTipoEvento(), Operacion.DELETE);
    }

    private TipoEvento buscar(Integer id) {
        return tipoEventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoEvento", id));
    }

    private void aplicar(TipoEventoRequest request, TipoEvento tipoEvento) {
        tipoEvento.setNombreTipo(request.nombreTipo().trim());
    }
}
