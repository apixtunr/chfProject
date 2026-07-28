package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.CostoEvento;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.TipoCosto;
import com.lacasadelchef.erp.evento.dto.CostoEventoRequest;
import com.lacasadelchef.erp.evento.dto.CostoEventoResponse;
import com.lacasadelchef.erp.repository.CostoEventoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.TipoCostoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CostoEventoServiceImpl implements CostoEventoService {

    private static final String TABLA = "costo_evento";

    private final CostoEventoRepository costoEventoRepository;
    private final EventoRepository eventoRepository;
    private final TipoCostoRepository tipoCostoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<CostoEventoResponse> listar(Integer idEvento) {
        return costoEventoRepository.findByEventoIdEvento(idEvento).stream()
                .map(CostoEventoResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public CostoEventoResponse agregar(Integer idEvento, CostoEventoRequest request) {
        Evento evento = buscarEvento(idEvento);
        CostoEvento costoEvento = new CostoEvento();
        costoEvento.setEvento(evento);
        aplicar(request, costoEvento);
        costoEvento = costoEventoRepository.save(costoEvento);
        bitacoraMovimientoService.registrar(TABLA, costoEvento.getIdCostoEvento(), Operacion.INSERT);
        return CostoEventoResponse.desde(costoEvento);
    }

    @Override
    @Transactional
    public CostoEventoResponse actualizar(Integer idEvento, Integer idCostoEvento, CostoEventoRequest request) {
        CostoEvento costoEvento = buscarCosto(idEvento, idCostoEvento);
        aplicar(request, costoEvento);
        costoEvento = costoEventoRepository.save(costoEvento);
        bitacoraMovimientoService.registrar(TABLA, costoEvento.getIdCostoEvento(), Operacion.UPDATE);
        return CostoEventoResponse.desde(costoEvento);
    }

    @Override
    @Transactional
    public void eliminar(Integer idEvento, Integer idCostoEvento) {
        CostoEvento costoEvento = buscarCosto(idEvento, idCostoEvento);
        costoEventoRepository.delete(costoEvento);
        bitacoraMovimientoService.registrar(TABLA, idCostoEvento, Operacion.DELETE);
    }

    private Evento buscarEvento(Integer id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", id));
    }

    private CostoEvento buscarCosto(Integer idEvento, Integer idCostoEvento) {
        CostoEvento costoEvento = costoEventoRepository.findById(idCostoEvento)
                .orElseThrow(() -> new ResourceNotFoundException("CostoEvento", idCostoEvento));
        if (!costoEvento.getEvento().getIdEvento().equals(idEvento)) {
            throw new ResourceNotFoundException("CostoEvento", idCostoEvento);
        }
        return costoEvento;
    }

    private void aplicar(CostoEventoRequest request, CostoEvento costoEvento) {
        TipoCosto tipoCosto = tipoCostoRepository.findById(request.idTipoCosto())
                .orElseThrow(() -> new ResourceNotFoundException("TipoCosto", request.idTipoCosto()));
        costoEvento.setTipoCosto(tipoCosto);
        costoEvento.setDescripcion(request.descripcion());
        costoEvento.setMonto(request.monto());
        if (request.fechaCosto() != null) {
            costoEvento.setFechaCosto(request.fechaCosto());
        }
    }
}
