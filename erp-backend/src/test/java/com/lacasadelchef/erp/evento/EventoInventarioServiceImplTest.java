package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.EventoInventario;
import com.lacasadelchef.erp.entity.Producto;
import com.lacasadelchef.erp.entity.id.EventoInventarioId;
import com.lacasadelchef.erp.evento.dto.EventoInventarioRequest;
import com.lacasadelchef.erp.inventario.MovimientoInventarioService;
import com.lacasadelchef.erp.repository.EventoInventarioRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Una linea de inventario ya descontada del stock no puede volver a "planificado" por la
 * puerta de atras: antes, agregar otra vez el mismo producto la sobrescribia sin devolver
 * lo descontado y, al confirmarla de nuevo, el stock bajaba dos veces.
 */
@ExtendWith(MockitoExtension.class)
class EventoInventarioServiceImplTest {

    private static final int ID_EVENTO = 44;
    private static final int ID_PRODUCTO = 14;

    @Mock private EventoInventarioRepository eventoInventarioRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private MovimientoInventarioService movimientoInventarioService;
    @Mock private EventoInventarioLineaConfirmador lineaConfirmador;

    @InjectMocks private EventoInventarioServiceImpl service;

    private Evento evento;
    private Producto harina;

    @BeforeEach
    void preparar() {
        evento = EventoAsignacionesTest.evento(ID_EVENTO, "CREADO", 11, 15);
        harina = new Producto();
        harina.setIdProducto(ID_PRODUCTO);
        harina.setNombreProducto("Harina para panqueques");
        lenient().when(eventoRepository.findById(ID_EVENTO)).thenReturn(Optional.of(evento));
        lenient().when(productoRepository.findById(ID_PRODUCTO)).thenReturn(Optional.of(harina));
        lenient().when(eventoInventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private EventoInventario linea(BigDecimal cantidad, LocalDateTime fechaConsumo) {
        EventoInventario linea = new EventoInventario();
        linea.setId(new EventoInventarioId(ID_EVENTO, ID_PRODUCTO));
        linea.setEvento(evento);
        linea.setProducto(harina);
        linea.setCantidad(cantidad);
        linea.setFechaConsumo(fechaConsumo);
        when(eventoInventarioRepository.findById(new EventoInventarioId(ID_EVENTO, ID_PRODUCTO)))
                .thenReturn(Optional.of(linea));
        return linea;
    }

    @Test
    @DisplayName("Agregar un producto ya descontado se rechaza y no lo regresa a planificado")
    void agregarProductoYaConsumidoSeRechaza() {
        EventoInventario consumida = linea(new BigDecimal("5"), LocalDateTime.now());

        assertThatThrownBy(() -> service.agregar(ID_EVENTO, ID_PRODUCTO,
                new EventoInventarioRequest(new BigDecimal("5"), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Corregir consumo");

        assertThat(consumida.getFechaConsumo()).isNotNull();
        verify(eventoInventarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Agregar un producto que solo estaba planificado actualiza su cantidad")
    void agregarProductoPlanificadoActualizaCantidad() {
        EventoInventario planificada = linea(new BigDecimal("5"), null);

        service.agregar(ID_EVENTO, ID_PRODUCTO, new EventoInventarioRequest(new BigDecimal("7"), null));

        assertThat(planificada.getCantidad()).isEqualByComparingTo("7");
        assertThat(planificada.getFechaConsumo()).isNull();
    }

    @Test
    @DisplayName("El cliente no puede fijar ni borrar la fecha de consumo al agregar")
    void agregarIgnoraLaFechaDeConsumoDelCliente() {
        when(eventoInventarioRepository.findById(new EventoInventarioId(ID_EVENTO, ID_PRODUCTO)))
                .thenReturn(Optional.empty());

        var respuesta = service.agregar(ID_EVENTO, ID_PRODUCTO,
                new EventoInventarioRequest(new BigDecimal("5"), LocalDateTime.now()));

        assertThat(respuesta.fechaConsumo()).isNull();
    }

    @Test
    @DisplayName("Una linea ya descontada no se puede quitar del evento")
    void quitarLineaConsumidaSeRechaza() {
        linea(new BigDecimal("5"), LocalDateTime.now());

        assertThatThrownBy(() -> service.quitar(ID_EVENTO, ID_PRODUCTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Corregir consumo");

        verify(eventoInventarioRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Una linea ya descontada no se edita: se usa Corregir consumo")
    void actualizarLineaConsumidaSeRechaza() {
        linea(new BigDecimal("5"), LocalDateTime.now());

        assertThatThrownBy(() -> service.actualizar(ID_EVENTO, ID_PRODUCTO,
                new EventoInventarioRequest(new BigDecimal("3"), null)))
                .isInstanceOf(BusinessException.class);
    }
}
