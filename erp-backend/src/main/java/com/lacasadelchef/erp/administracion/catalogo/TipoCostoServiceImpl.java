package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoCostoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoCostoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.TipoCosto;
import com.lacasadelchef.erp.repository.TipoCostoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoCostoServiceImpl implements TipoCostoService {

    private static final String TABLA = "tipo_costo";

    private final TipoCostoRepository tipoCostoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<TipoCostoResponse> listar() {
        return tipoCostoRepository.findAll().stream().map(TipoCostoResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoCostoResponse obtenerPorId(Integer id) {
        return TipoCostoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public TipoCostoResponse crear(TipoCostoRequest request) {
        TipoCosto tipoCosto = new TipoCosto();
        aplicar(request, tipoCosto);
        tipoCosto = tipoCostoRepository.save(tipoCosto);
        bitacoraMovimientoService.registrar(TABLA, tipoCosto.getIdTipoCosto(), Operacion.INSERT);
        return TipoCostoResponse.desde(tipoCosto);
    }

    @Override
    @Transactional
    public TipoCostoResponse actualizar(Integer id, TipoCostoRequest request) {
        TipoCosto tipoCosto = buscar(id);
        aplicar(request, tipoCosto);
        tipoCosto = tipoCostoRepository.save(tipoCosto);
        return TipoCostoResponse.desde(tipoCosto);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        TipoCosto tipoCosto = buscar(id);
        tipoCostoRepository.delete(tipoCosto);
        bitacoraMovimientoService.registrar(TABLA, tipoCosto.getIdTipoCosto(), Operacion.DELETE);
    }

    private TipoCosto buscar(Integer id) {
        return tipoCostoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoCosto", id));
    }

    private void aplicar(TipoCostoRequest request, TipoCosto tipoCosto) {
        tipoCosto.setNombreTipo(request.nombreTipo().trim());
        tipoCosto.setDescripcion(request.descripcion());
    }
}
