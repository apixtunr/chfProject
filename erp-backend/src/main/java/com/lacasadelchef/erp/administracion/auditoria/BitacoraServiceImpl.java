package com.lacasadelchef.erp.administracion.auditoria;

import com.lacasadelchef.erp.administracion.auditoria.dto.BitacoraAccesoResponse;
import com.lacasadelchef.erp.administracion.auditoria.dto.BitacoraMovimientoResponse;
import com.lacasadelchef.erp.repository.BitacoraAccesoRepository;
import com.lacasadelchef.erp.repository.BitacoraMovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Solo lectura: bitacora_movimiento y bitacora_acceso se llenan desde
 * BitacoraMovimientoService y AuthService respectivamente; este servicio unicamente
 * las consulta para la pantalla de Bitacora.
 */
@Service
@RequiredArgsConstructor
public class BitacoraServiceImpl implements BitacoraService {

    private final BitacoraMovimientoRepository bitacoraMovimientoRepository;
    private final BitacoraAccesoRepository bitacoraAccesoRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BitacoraMovimientoResponse> listarMovimientos(Integer idUsuario, String tabla,
                                                                LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
        return bitacoraMovimientoRepository.buscar(idUsuario, tabla, fechaDesde, fechaHasta, pageable)
                .map(BitacoraMovimientoResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BitacoraAccesoResponse> listarAccesos(Integer idUsuario, String resultado,
                                                       LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
        return bitacoraAccesoRepository.buscar(idUsuario, resultado, fechaDesde, fechaHasta, pageable)
                .map(BitacoraAccesoResponse::desde);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listarTablasConMovimientos() {
        return bitacoraMovimientoRepository.listarTablasDistintas();
    }
}
