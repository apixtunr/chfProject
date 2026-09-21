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

/**
 * Confirma una sola linea de evento_inventario (registra la SALIDA y marca su fecha de
 * consumo) en su propia transaccion (REQUIRES_NEW). Es un bean aparte, y no un metodo mas
 * de EventoInventarioServiceImpl, por la misma razon documentada en
 * {@link EventoEstadoTransicionExecutor}: si esto se invocara como metodo privado dentro de
 * un @Transactional ya abierto (confirmarConsumoAutomatico o confirmarTodo, que recorren
 * varias lineas atrapando el BusinessException de cada una para seguir con las demas), el
 * fallo de una linea marcaria toda esa transaccion compartida como rollback-only pese a
 * quedar "atrapado" en el catch, y el commit final terminaria lanzando
 * UnexpectedRollbackException y deshaciendo tambien las lineas que si se confirmaron. Con
 * REQUIRES_NEW, cada linea vive en su propia transaccion: si falla, solo ella se revierte.
 */
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
