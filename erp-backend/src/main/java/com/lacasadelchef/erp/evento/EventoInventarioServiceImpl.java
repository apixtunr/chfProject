package com.lacasadelchef.erp.evento;

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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventoInventarioServiceImpl implements EventoInventarioService {

    private static final String TIPO_AJUSTE = "AJUSTE";

    private final EventoInventarioRepository eventoInventarioRepository;
    private final EventoRepository eventoRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioService movimientoInventarioService;
    private final EventoInventarioLineaConfirmador lineaConfirmador;

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
        validarNoCancelado(evento);

        // Si el producto ya esta en el evento, guardar una linea nueva con la misma llave la
        // sobrescribia: volvia a "planificado" sin devolver al stock lo ya descontado, y al
        // confirmarla otra vez se descontaba dos veces. Ahora se respeta lo que ya existe.
        var existente = eventoInventarioRepository.findById(new EventoInventarioId(idEvento, idProducto));
        if (existente.isPresent()) {
            EventoInventario linea = existente.get();
            validarNoConsumido(linea, "volver a agregarlo");
            linea.setCantidad(request.cantidad());
            return EventoInventarioResponse.desde(eventoInventarioRepository.save(linea));
        }

        EventoInventario eventoInventario = new EventoInventario();
        eventoInventario.setId(new EventoInventarioId(idEvento, idProducto));
        eventoInventario.setEvento(evento);
        eventoInventario.setProducto(producto);
        eventoInventario.setCantidad(request.cantidad());
        eventoInventario = eventoInventarioRepository.save(eventoInventario);
        return EventoInventarioResponse.desde(eventoInventario);
    }

    @Override
    @Transactional
    public EventoInventarioResponse actualizar(Integer idEvento, Integer idProducto, EventoInventarioRequest request) {
        EventoInventario eventoInventario = buscar(idEvento, idProducto);
        validarNoConsumido(eventoInventario, "cambiar la cantidad");
        eventoInventario.setCantidad(request.cantidad());
        eventoInventario = eventoInventarioRepository.save(eventoInventario);
        return EventoInventarioResponse.desde(eventoInventario);
    }

    @Override
    @Transactional
    public void quitar(Integer idEvento, Integer idProducto) {
        EventoInventario eventoInventario = buscar(idEvento, idProducto);
        validarNoConsumido(eventoInventario, "quitarlo");
        eventoInventarioRepository.delete(eventoInventario);
    }

    @Override
    @Transactional
    public EventoInventarioResponse confirmarConsumo(Integer idEvento, Integer idProducto) {
        EventoInventario eventoInventario = buscar(idEvento, idProducto);
        validarNoCancelado(eventoInventario.getEvento());
        if (eventoInventario.getFechaConsumo() != null) {
            throw new BusinessException(
                    "El consumo de este producto ya fue confirmado para este evento el %s"
                            .formatted(eventoInventario.getFechaConsumo()));
        }
        lineaConfirmador.confirmar(eventoInventario, idEvento, "Consumo del evento #%d".formatted(idEvento));
        return EventoInventarioResponse.desde(eventoInventario);
    }

    @Override
    @Transactional
    public void confirmarConsumoAutomatico(Integer idEvento) {
        String motivo = "Consumo automatico al iniciar el evento #%d".formatted(idEvento);
        for (EventoInventario item : eventoInventarioRepository.findByEventoIdEventoAndFechaConsumoIsNull(idEvento)) {
            try {
                lineaConfirmador.confirmar(item, idEvento, motivo);
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
                lineaConfirmador.confirmar(item, idEvento, motivo);
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
        return EventoInventarioResponse.desde(item);
    }

    private EventoInventario buscar(Integer idEvento, Integer idProducto) {
        return eventoInventarioRepository.findById(new EventoInventarioId(idEvento, idProducto))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El producto %d no esta asociado al evento %d".formatted(idProducto, idEvento)));
    }

    /**
     * Una linea ya descontada del stock solo se cambia con "Corregir consumo", que ajusta el
     * stock por la diferencia. La fecha de consumo nunca la manda el cliente: la pone el
     * sistema al confirmar.
     */
    private static void validarNoConsumido(EventoInventario linea, String accion) {
        if (linea.getFechaConsumo() != null) {
            throw new BusinessException(("%s ya se descontó del stock (%s): para %s use \"Corregir consumo\","
                    + " que ajusta el stock por la diferencia").formatted(linea.getProducto().getNombreProducto(),
                    linea.getCantidad().stripTrailingZeros().toPlainString(), accion));
        }
    }

    /** Un evento cancelado ya no usa inventario: no se planifica ni se descuenta nada mas. */
    private static void validarNoCancelado(Evento evento) {
        if (EventoReglas.ESTADO_CANCELADO.equalsIgnoreCase(evento.getEstado().getNombre())) {
            throw new BusinessException("El evento está CANCELADO: ya no se le asigna ni descuenta inventario");
        }
    }
}
