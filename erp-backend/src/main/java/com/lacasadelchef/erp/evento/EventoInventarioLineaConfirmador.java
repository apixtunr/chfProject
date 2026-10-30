package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.entity.EventoInventario;
import com.lacasadelchef.erp.inventario.MovimientoInventarioService;
import com.lacasadelchef.erp.inventario.dto.MovimientoInventarioRequest;
import com.lacasadelchef.erp.repository.EventoInventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
class EventoInventarioLineaConfirmador {

    private static final String TIPO_SALIDA = "SALIDA";

    private final EventoInventarioRepository eventoInventarioRepository;
    private final MovimientoInventarioService movimientoInventarioService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmar(EventoInventario eventoInventario, Integer idEvento, String motivo) {
        movimientoInventarioService.registrar(new MovimientoInventarioRequest(
                eventoInventario.getProducto().getIdProducto(),
                TIPO_SALIDA,
                eventoInventario.getCantidad(),
                motivo,
                idEvento));

        eventoInventario.setFechaConsumo(LocalDateTime.now());
        eventoInventarioRepository.save(eventoInventario);
    }
}
