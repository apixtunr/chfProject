package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.EventoInventario;
import com.lacasadelchef.erp.entity.Producto;
import com.lacasadelchef.erp.entity.id.EventoInventarioId;
import com.lacasadelchef.erp.evento.dto.EventoInventarioConfirmacionMasivaResponse;
import com.lacasadelchef.erp.evento.dto.EventoInventarioCorreccionRequest;
import com.lacasadelchef.erp.evento.dto.EventoInventarioFalloResponse;
import com.lacasadelchef.erp.evento.dto.EventoInventarioRequest;
import com.lacasadelchef.erp.evento.dto.EventoInventarioResponse;
import com.lacasadelchef.erp.inventario.MovimientoInventarioService;
import com.lacasadelchef.erp.inventario.dto.MovimientoInventarioRequest;
import com.lacasadelchef.erp.repository.EventoInventarioRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventoInventarioServiceImpl implements EventoInventarioService {

    private static final String TABLA = "evento_inventario";
    private static final String TIPO_SALIDA = "SALIDA";
    private static final String TIPO_AJUSTE = "AJUSTE";

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
        confirmarUnaLinea(eventoInventario, idEvento, "Consumo del evento #%d".formatted(idEvento));
        return EventoInventarioResponse.desde(eventoInventario);
    }

    @Override
    @Transactional
    public void confirmarConsumoAutomatico(Integer idEvento) {
        String motivo = "Consumo automatico al iniciar el evento #%d".formatted(idEvento);
        for (EventoInventario item : eventoInventarioRepository.findByEventoIdEventoAndFechaConsumoIsNull(idEvento)) {
            try {
                confirmarUnaLinea(item, idEvento, motivo);
            } catch (BusinessException e) {
                // Sin un usuario presente para decidir que hacer (esto corre desde el scheduler),
                // se omite ese producto y se sigue con los demas en vez de bloquear al evento.
                log.warn("No se pudo confirmar consumo automatico del producto {} en el evento {}: {}",
                        item.getProducto().getIdProducto(), idEvento, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public EventoInventarioConfirmacionMasivaResponse confirmarTodo(Integer idEvento) {
        String motivo = "Consumo confirmado evento id %d".formatted(idEvento);
        List<EventoInventarioResponse> confirmados = new ArrayList<>();
        List<EventoInventarioFalloResponse> fallidos = new ArrayList<>();
        for (EventoInventario item : eventoInventarioRepository.findByEventoIdEventoAndFechaConsumoIsNull(idEvento)) {
            try {
                confirmarUnaLinea(item, idEvento, motivo);
                confirmados.add(EventoInventarioResponse.desde(item));
            } catch (BusinessException e) {
                fallidos.add(new EventoInventarioFalloResponse(item.getProducto().getNombreProducto(), e.getMessage()));
            }
        }
        return new EventoInventarioConfirmacionMasivaResponse(confirmados, fallidos);
    }

    @Override
    @Transactional
    public EventoInventarioResponse corregirConsumo(Integer idEvento, Integer idProducto,
                                                     EventoInventarioCorreccionRequest request) {
        EventoInventario item = buscar(idEvento, idProducto);
        if (item.getFechaConsumo() == null) {
            throw new BusinessException(
                    "Este producto todavia no se ha confirmado; edite la cantidad planificada en vez de corregirla");
        }
        BigDecimal cantidadAnterior = item.getCantidad();
        BigDecimal cantidadCorrecta = request.cantidadCorrecta();
        // Cuanto hay que devolverle al stock (positivo) o quitarle de mas (negativo) para que
        // el stock real quede como si desde el principio se hubiera confirmado la cantidad
        // correcta, sin borrar el movimiento SALIDA original del historial.
        BigDecimal ajuste = cantidadAnterior.subtract(cantidadCorrecta);
        if (ajuste.compareTo(BigDecimal.ZERO) != 0) {
            movimientoInventarioService.registrar(new MovimientoInventarioRequest(
                    idProducto,
                    TIPO_AJUSTE,
                    ajuste,
                    "Correccion consumo evento id %d: de %s a %s".formatted(idEvento, cantidadAnterior, cantidadCorrecta),
                    idEvento));
        }
        item.setCantidad(cantidadCorrecta);
        eventoInventarioRepository.save(item);
        bitacoraMovimientoService.registrar(TABLA, idEvento + "-" + idProducto, Operacion.UPDATE);
        return EventoInventarioResponse.desde(item);
    }

    // Genera la salida de stock real recien ahora; mientras solo estaba "planificado"
    // (fecha_consumo en blanco) no tocaba el inventario.
    private void confirmarUnaLinea(EventoInventario eventoInventario, Integer idEvento, String motivo) {
        movimientoInventarioService.registrar(new MovimientoInventarioRequest(
                eventoInventario.getProducto().getIdProducto(),
                TIPO_SALIDA,
                eventoInventario.getCantidad(),
                motivo,
                idEvento));

        eventoInventario.setFechaConsumo(LocalDateTime.now());
        eventoInventarioRepository.save(eventoInventario);
        bitacoraMovimientoService.registrar(
                TABLA, idEvento + "-" + eventoInventario.getProducto().getIdProducto(), Operacion.UPDATE);
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
