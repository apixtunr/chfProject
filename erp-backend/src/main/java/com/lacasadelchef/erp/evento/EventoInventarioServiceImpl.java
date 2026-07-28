package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.EventoInventario;
import com.lacasadelchef.erp.entity.Producto;
import com.lacasadelchef.erp.entity.id.EventoInventarioId;
import com.lacasadelchef.erp.evento.dto.EventoInventarioRequest;
import com.lacasadelchef.erp.evento.dto.EventoInventarioResponse;
import com.lacasadelchef.erp.inventario.MovimientoInventarioService;
import com.lacasadelchef.erp.inventario.dto.MovimientoInventarioRequest;
import com.lacasadelchef.erp.repository.EventoInventarioRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoInventarioServiceImpl implements EventoInventarioService {

    private static final String TABLA = "evento_inventario";
    private static final String TIPO_SALIDA = "SALIDA";

    private final EventoInventarioRepository eventoInventarioRepository;
    private final EventoRepository eventoRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioService movimientoInventarioService;
    private final BitacoraMovimientoService bitacoraMovimientoService;

    @Override
    @Transactional(readOnly = true)
    public List<EventoInventarioResponse> listar(Integer idEvento) {
        return eventoInventarioRepository.findByEventoIdEvento(idEvento).stream()
                .map(EventoInventarioResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public EventoInventarioResponse agregar(Integer idEvento, Integer idProducto, EventoInventarioRequest request) {
        Evento evento = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", idEvento));
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", idProducto));

        EventoInventario eventoInventario = new EventoInventario();
        eventoInventario.setId(new EventoInventarioId(idEvento, idProducto));
        eventoInventario.setEvento(evento);
        eventoInventario.setProducto(producto);
        aplicar(request, eventoInventario);
        eventoInventario = eventoInventarioRepository.save(eventoInventario);
        bitacoraMovimientoService.registrar(TABLA, idEvento + "-" + idProducto, Operacion.INSERT);
        return EventoInventarioResponse.desde(eventoInventario);
    }

    @Override
    @Transactional
    public EventoInventarioResponse actualizar(Integer idEvento, Integer idProducto, EventoInventarioRequest request) {
        EventoInventario eventoInventario = buscar(idEvento, idProducto);
        aplicar(request, eventoInventario);
        eventoInventario = eventoInventarioRepository.save(eventoInventario);
        bitacoraMovimientoService.registrar(TABLA, idEvento + "-" + idProducto, Operacion.UPDATE);
        return EventoInventarioResponse.desde(eventoInventario);
    }

    @Override
    @Transactional
    public void quitar(Integer idEvento, Integer idProducto) {
        EventoInventario eventoInventario = buscar(idEvento, idProducto);
        eventoInventarioRepository.delete(eventoInventario);
        bitacoraMovimientoService.registrar(TABLA, idEvento + "-" + idProducto, Operacion.DELETE);
    }

    @Override
    @Transactional
    public EventoInventarioResponse confirmarConsumo(Integer idEvento, Integer idProducto) {
        EventoInventario eventoInventario = buscar(idEvento, idProducto);
        if (eventoInventario.getFechaConsumo() != null) {
            throw new BusinessException(
                    "El consumo de este producto ya fue confirmado para este evento el %s"
                            .formatted(eventoInventario.getFechaConsumo()));
        }

        // Genera la salida de stock real recien ahora; mientras solo estaba "planificado"
        // (fecha_consumo en blanco) no tocaba el inventario.
        movimientoInventarioService.registrar(new MovimientoInventarioRequest(
                idProducto,
                TIPO_SALIDA,
                eventoInventario.getCantidad(),
                "Consumo del evento #%d".formatted(idEvento),
                idEvento));

        eventoInventario.setFechaConsumo(LocalDateTime.now());
        eventoInventario = eventoInventarioRepository.save(eventoInventario);
        bitacoraMovimientoService.registrar(TABLA, idEvento + "-" + idProducto, Operacion.UPDATE);
        return EventoInventarioResponse.desde(eventoInventario);
    }

    private EventoInventario buscar(Integer idEvento, Integer idProducto) {
        return eventoInventarioRepository.findById(new EventoInventarioId(idEvento, idProducto))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El producto %d no esta asociado al evento %d".formatted(idProducto, idEvento)));
    }

    private void aplicar(EventoInventarioRequest request, EventoInventario eventoInventario) {
        eventoInventario.setCantidad(request.cantidad());
        eventoInventario.setFechaConsumo(request.fechaConsumo());
    }
}
