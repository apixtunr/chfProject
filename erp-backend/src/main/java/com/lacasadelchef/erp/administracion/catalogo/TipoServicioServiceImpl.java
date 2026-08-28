package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoServicioRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoServicioResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.TipoServicio;
import com.lacasadelchef.erp.repository.TipoServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoServicioServiceImpl implements TipoServicioService {

    private static final String TABLA = "tipo_servicio";

    private final TipoServicioRepository tipoServicioRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<TipoServicioResponse> listar() {
        return tipoServicioRepository.findAll().stream().map(TipoServicioResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoServicioResponse obtenerPorId(Integer id) {
        return TipoServicioResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public TipoServicioResponse crear(TipoServicioRequest request) {
        TipoServicio tipoServicio = new TipoServicio();
        aplicar(request, tipoServicio);
        tipoServicio = tipoServicioRepository.save(tipoServicio);
        bitacoraMovimientoService.registrar(TABLA, tipoServicio.getIdTipoServicio(), Operacion.INSERT);
        return TipoServicioResponse.desde(tipoServicio);
    }

    @Override
    @Transactional
    public TipoServicioResponse actualizar(Integer id, TipoServicioRequest request) {
        TipoServicio tipoServicio = buscar(id);
        aplicar(request, tipoServicio);
        tipoServicio = tipoServicioRepository.save(tipoServicio);
        bitacoraMovimientoService.registrar(TABLA, tipoServicio.getIdTipoServicio(), Operacion.UPDATE);
        return TipoServicioResponse.desde(tipoServicio);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        TipoServicio tipoServicio = buscar(id);
        tipoServicioRepository.delete(tipoServicio);
        bitacoraMovimientoService.registrar(TABLA, tipoServicio.getIdTipoServicio(), Operacion.DELETE);
    }

    private TipoServicio buscar(Integer id) {
        return tipoServicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoServicio", id));
    }

    private void aplicar(TipoServicioRequest request, TipoServicio tipoServicio) {
        tipoServicio.setNombreTipo(request.nombreTipo().trim());
        tipoServicio.setDescripcion(request.descripcion());
    }
}
