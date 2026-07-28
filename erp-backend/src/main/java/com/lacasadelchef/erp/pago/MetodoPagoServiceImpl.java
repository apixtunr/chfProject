package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.MetodoPago;
import com.lacasadelchef.erp.pago.dto.MetodoPagoRequest;
import com.lacasadelchef.erp.pago.dto.MetodoPagoResponse;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.MetodoPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetodoPagoServiceImpl implements MetodoPagoService {

    private static final String TABLA = "metodo_pago";

    private final MetodoPagoRepository metodoPagoRepository;
    private final EstadoRepository estadoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<MetodoPagoResponse> listar() {
        return metodoPagoRepository.findAll().stream()
                .map(MetodoPagoResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MetodoPagoResponse obtenerPorId(Integer id) {
        return MetodoPagoResponse.desde(buscar(id));
    }

    @Override
    @Transactional
    public MetodoPagoResponse crear(MetodoPagoRequest request) {
        MetodoPago metodoPago = new MetodoPago();
        aplicar(request, metodoPago);
        metodoPago = metodoPagoRepository.save(metodoPago);
        bitacoraMovimientoService.registrar(TABLA, metodoPago.getIdMetodoPago(), Operacion.INSERT);
        return MetodoPagoResponse.desde(metodoPago);
    }

    @Override
    @Transactional
    public MetodoPagoResponse actualizar(Integer id, MetodoPagoRequest request) {
        MetodoPago metodoPago = buscar(id);
        aplicar(request, metodoPago);
        metodoPago = metodoPagoRepository.save(metodoPago);
        bitacoraMovimientoService.registrar(TABLA, metodoPago.getIdMetodoPago(), Operacion.UPDATE);
        return MetodoPagoResponse.desde(metodoPago);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        MetodoPago metodoPago = buscar(id);
        metodoPagoRepository.delete(metodoPago);
        bitacoraMovimientoService.registrar(TABLA, metodoPago.getIdMetodoPago(), Operacion.DELETE);
    }

    private MetodoPago buscar(Integer id) {
        return metodoPagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MetodoPago", id));
    }

    private void aplicar(MetodoPagoRequest request, MetodoPago metodoPago) {
        Estado estado = estadoRepository.findById(request.idEstado())
                .orElseThrow(() -> new ResourceNotFoundException("Estado", request.idEstado()));
        metodoPago.setEstado(estado);
        metodoPago.setNombreMetodo(request.nombreMetodo().trim());
        metodoPago.setDescripcion(request.descripcion());
        metodoPago.setRequiereReferencia(request.requiereReferencia());
    }
}
