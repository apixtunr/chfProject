package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.TipoInventario;
import com.lacasadelchef.erp.inventario.dto.TipoInventarioRequest;
import com.lacasadelchef.erp.inventario.dto.TipoInventarioResponse;
import com.lacasadelchef.erp.repository.TipoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoInventarioServiceImpl implements TipoInventarioService {

    private static final String TABLA = "tipo_inventario";

    private final TipoInventarioRepository tipoInventarioRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<TipoInventarioResponse> listar() {
        return tipoInventarioRepository.findAll().stream()
                .map(TipoInventarioResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoInventarioResponse obtenerPorId(Integer id) {
        return TipoInventarioResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public TipoInventarioResponse crear(TipoInventarioRequest request) {
        TipoInventario tipoInventario = new TipoInventario();
        aplicar(request, tipoInventario);
        tipoInventario = tipoInventarioRepository.save(tipoInventario);
        bitacoraMovimientoService.registrar(TABLA, tipoInventario.getIdTipoInventario(), Operacion.INSERT);
        return TipoInventarioResponse.desde(tipoInventario);
    }

    @Override
    @Transactional
    public TipoInventarioResponse actualizar(Integer id, TipoInventarioRequest request) {
        TipoInventario tipoInventario = buscar(id);
        aplicar(request, tipoInventario);
        tipoInventario = tipoInventarioRepository.save(tipoInventario);
        return TipoInventarioResponse.desde(tipoInventario);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        TipoInventario tipoInventario = buscar(id);
        tipoInventarioRepository.delete(tipoInventario);
        bitacoraMovimientoService.registrar(TABLA, tipoInventario.getIdTipoInventario(), Operacion.DELETE);
    }

    private TipoInventario buscar(Integer id) {
        return tipoInventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoInventario", id));
    }

    private void aplicar(TipoInventarioRequest request, TipoInventario tipoInventario) {
        tipoInventario.setNombreTipo(request.nombreTipo().trim());
        tipoInventario.setDescripcion(request.descripcion());
    }
}
