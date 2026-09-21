package com.lacasadelchef.erp.common.audit;

import com.lacasadelchef.erp.entity.BitacoraMovimiento;
import com.lacasadelchef.erp.repository.BitacoraMovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra en bitacora_movimiento una fila por cada INSERT o DELETE de negocio; se invoca
 * explicitamente desde cada service. Los UPDATE no se registran por aqui: los captura
 * {@link AuditoriaCambiosListener}, campo por campo, con el valor anterior y el nuevo.
 */
@Service
@RequiredArgsConstructor
public class BitacoraMovimientoService {

    private final BitacoraMovimientoRepository bitacoraMovimientoRepository;

    @Transactional
    public void registrar(String tablaAfectada, Object registroId, Operacion operacion) {
        if (operacion == Operacion.UPDATE) {
            throw new IllegalArgumentException(
                    "Los UPDATE los registra AuditoriaCambiosListener; no hace falta llamarlo a mano");
        }
        BitacoraMovimiento registro = new BitacoraMovimiento();
        registro.setUsuario(ContextoAuditoria.usuarioActual());
        registro.setTablaAfectada(tablaAfectada);
        registro.setRegistroId(registroId == null ? null : String.valueOf(registroId));
        registro.setOperacion(operacion.name());
        registro.setIpOrigen(ContextoAuditoria.ipActual());
        bitacoraMovimientoRepository.save(registro);
    }
}
