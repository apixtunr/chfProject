package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.ComprobantePago;
import com.lacasadelchef.erp.entity.Pago;
import com.lacasadelchef.erp.pago.dto.ComprobantePagoRequest;
import com.lacasadelchef.erp.pago.dto.ComprobantePagoResponse;
import com.lacasadelchef.erp.repository.ComprobantePagoRepository;
import com.lacasadelchef.erp.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprobantePagoServiceImpl implements ComprobantePagoService {

    private static final String TABLA = "comprobante_pago";

    private final ComprobantePagoRepository comprobantePagoRepository;
    private final PagoRepository pagoRepository;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<ComprobantePagoResponse> listar(Integer idPago) {
        return comprobantePagoRepository.findByPagoIdPago(idPago).stream()
                .map(ComprobantePagoResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public ComprobantePagoResponse agregar(Integer idPago, ComprobantePagoRequest request) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", idPago));
        ComprobantePago comprobante = new ComprobantePago();
        comprobante.setPago(pago);
        aplicar(request, comprobante);
        comprobante = comprobantePagoRepository.save(comprobante);
        bitacoraMovimientoService.registrar(TABLA, comprobante.getIdComprobante(), Operacion.INSERT);
        return ComprobantePagoResponse.desde(comprobante);
    }

    @Override
    @Transactional
    public ComprobantePagoResponse actualizar(Integer idPago, Integer idComprobante, ComprobantePagoRequest request) {
        ComprobantePago comprobante = buscar(idPago, idComprobante);
        aplicar(request, comprobante);
        comprobante = comprobantePagoRepository.save(comprobante);
        bitacoraMovimientoService.registrar(TABLA, comprobante.getIdComprobante(), Operacion.UPDATE);
        return ComprobantePagoResponse.desde(comprobante);
    }

    @Override
    @Transactional
    public void eliminar(Integer idPago, Integer idComprobante) {
        ComprobantePago comprobante = buscar(idPago, idComprobante);
        comprobantePagoRepository.delete(comprobante);
        bitacoraMovimientoService.registrar(TABLA, idComprobante, Operacion.DELETE);
    }

    private ComprobantePago buscar(Integer idPago, Integer idComprobante) {
        ComprobantePago comprobante = comprobantePagoRepository.findById(idComprobante)
                .orElseThrow(() -> new ResourceNotFoundException("ComprobantePago", idComprobante));
        if (!comprobante.getPago().getIdPago().equals(idPago)) {
            throw new ResourceNotFoundException("ComprobantePago", idComprobante);
        }
        return comprobante;
    }

    private void aplicar(ComprobantePagoRequest request, ComprobantePago comprobante) {
        comprobante.setNumeroComprobante(request.numeroComprobante().trim());
        comprobante.setArchivoUrl(request.archivoUrl());
        comprobante.setTipoComprobante(request.tipoComprobante());
        comprobante.setFechaEmision(request.fechaEmision());
        comprobante.setEsValido(request.esValido() == null || request.esValido());
    }
}
