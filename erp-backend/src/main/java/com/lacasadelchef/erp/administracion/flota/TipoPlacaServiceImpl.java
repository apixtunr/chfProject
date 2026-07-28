package com.lacasadelchef.erp.administracion.flota;

import com.lacasadelchef.erp.administracion.flota.dto.TipoPlacaRequest;
import com.lacasadelchef.erp.administracion.flota.dto.TipoPlacaResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.TipoPlaca;
import com.lacasadelchef.erp.repository.TipoPlacaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoPlacaServiceImpl implements TipoPlacaService {

    private static final String TABLA = "tipo_placa";

    private final TipoPlacaRepository tipoPlacaRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<TipoPlacaResponse> listar() {
        return tipoPlacaRepository.findAll().stream().map(TipoPlacaResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TipoPlacaResponse obtenerPorId(Integer id) {
        return TipoPlacaResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public TipoPlacaResponse crear(TipoPlacaRequest request) {
        TipoPlaca tipoPlaca = new TipoPlaca();
        aplicar(request, tipoPlaca);
        tipoPlaca = tipoPlacaRepository.save(tipoPlaca);
        bitacoraMovimientoService.registrar(TABLA, tipoPlaca.getIdTipoPlaca(), Operacion.INSERT);
        return TipoPlacaResponse.desde(tipoPlaca);
    }

    @Override
    @Transactional
    public TipoPlacaResponse actualizar(Integer id, TipoPlacaRequest request) {
        TipoPlaca tipoPlaca = buscar(id);
        aplicar(request, tipoPlaca);
        tipoPlaca = tipoPlacaRepository.save(tipoPlaca);
        bitacoraMovimientoService.registrar(TABLA, tipoPlaca.getIdTipoPlaca(), Operacion.UPDATE);
        return TipoPlacaResponse.desde(tipoPlaca);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        TipoPlaca tipoPlaca = buscar(id);
        tipoPlacaRepository.delete(tipoPlaca);
        bitacoraMovimientoService.registrar(TABLA, tipoPlaca.getIdTipoPlaca(), Operacion.DELETE);
    }

    private TipoPlaca buscar(Integer id) {
        return tipoPlacaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoPlaca", id));
    }

    private void aplicar(TipoPlacaRequest request, TipoPlaca tipoPlaca) {
        tipoPlaca.setNombreTipo(request.nombreTipo().trim());
    }
}
