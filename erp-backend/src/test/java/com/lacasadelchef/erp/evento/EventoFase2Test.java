package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.entity.TipoEvento;
import com.lacasadelchef.erp.entity.Ubicacion;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.entity.VPagoEvento;
import com.lacasadelchef.erp.evento.dto.CancelarEventoRequest;
import com.lacasadelchef.erp.evento.dto.EventoRequest;
import com.lacasadelchef.erp.evento.dto.EventoResponse;
import com.lacasadelchef.erp.repository.ClienteRepository;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.DetalleEventoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.EventoEmpleadoRepository;
import com.lacasadelchef.erp.repository.EventoInventarioRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.EventoVehiculoRepository;
import com.lacasadelchef.erp.repository.TipoEventoRepository;
import com.lacasadelchef.erp.repository.UbicacionRepository;
import com.lacasadelchef.erp.repository.VPagoEventoRepository;
import com.lacasadelchef.erp.security.UsuarioPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoFase2Test {

    @Mock private EventoRepository eventoRepository;
    @Mock private CotizacionVersionRepository cotizacionVersionRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private TipoEventoRepository tipoEventoRepository;
    @Mock private UbicacionRepository ubicacionRepository;
    @Mock private EstadoRepository estadoRepository;
    @Mock private DetalleEventoRepository detalleEventoRepository;
    @Mock private EventoEmpleadoRepository eventoEmpleadoRepository;
    @Mock private EventoVehiculoRepository eventoVehiculoRepository;
    @Mock private EventoInventarioRepository eventoInventarioRepository;
    @Mock private EventoEstadoSchedulerService estadoSchedulerService;
    @Mock private VPagoEventoRepository vPagoEventoRepository;
    @InjectMocks private EventoServiceImpl eventoService;

    private Evento evento;

    @BeforeEach
    void eventoCreado() {
        evento = new Evento();
        evento.setIdEvento(42);
        evento.setEstado(EventoAsignacionesTest.estado("CREADO"));
        Cliente cliente = new Cliente();
        cliente.setIdCliente(2);
        cliente.setNombre("Alejandra Ramírez");
        evento.setCliente(cliente);
        TipoEvento boda = new TipoEvento();
        boda.setIdTipoEvento(1);
        boda.setNombreTipo("Boda");
        evento.setTipoEvento(boda);
        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setIdUbicacion(1);
        evento.setUbicacion(ubicacion);
        lenient().when(eventoRepository.findById(42)).thenReturn(Optional.of(evento));
        lenient().when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));
        for (String nombre : List.of("PLANIFICADO", "CANCELADO", "CREADO")) {
            lenient().when(estadoRepository.findByTipoEstadoNombreTipoAndNombre("EVENTO", nombre))
                    .thenReturn(Optional.of(EventoAsignacionesTest.estado(nombre)));
        }
    }

    @AfterEach
    void sinSesion() {
        SecurityContextHolder.clearContext();
    }

    private void pagos(String total, String abonado) {
        VPagoEvento v = new VPagoEvento();
        ReflectionTestUtils.setField(v, "total", new BigDecimal(total));
        ReflectionTestUtils.setField(v, "abonado", new BigDecimal(abonado));
        when(vPagoEventoRepository.findById(42)).thenReturn(Optional.of(v));
    }

    private void completo() {
        when(detalleEventoRepository.existsByEventoIdEvento(42)).thenReturn(true);
        when(eventoEmpleadoRepository.existsByEventoIdEvento(42)).thenReturn(true);
        when(eventoVehiculoRepository.existsByEventoIdEvento(42)).thenReturn(true);
        when(eventoInventarioRepository.existsByEventoIdEvento(42)).thenReturn(true);
    }

    private void comoAdministrador() {
        Rol rol = new Rol();
        rol.setNombreRol("ADMINISTRADOR");
        Usuario usuario = new Usuario();
        usuario.setRol(rol);
        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @Test
    @DisplayName("Planificar sin el 50% pide confirmación y dice cuánto falta")
    void planificarSinAnticipoPideConfirmar() {
        completo();
        pagos("4000.00", "500.00");

        assertThatThrownBy(() -> eventoService.planificar(42, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ha pagado Q500.00 de Q4000.00; falta Q1500.00");
    }

    @Test
    @DisplayName("Si el usuario confirma, se planifica y queda marcado como planificado sin anticipo")
    void planificarConfirmado() {
        completo();
        pagos("4000.00", "500.00");

        EventoResponse r = eventoService.planificar(42, true);

        assertThat(r.estadoNombre()).isEqualTo("PLANIFICADO");
        assertThat(r.planificadoSinAnticipo()).isTrue();
    }

    @Test
    @DisplayName("Con el 50% pagado se planifica sin preguntar")
    void planificarConAnticipo() {
        completo();
        pagos("4000.00", "2000.00");

        EventoResponse r = eventoService.planificar(42, false);

        assertThat(r.planificadoSinAnticipo()).isFalse();
    }

    @Test
    @DisplayName("Cancelar con pagos exige decir en qué quedó el anticipo")
    void cancelarSinAcuerdo() {
        comoAdministrador();
        pagos("4000.00", "2000.00");

        assertThatThrownBy(() -> eventoService.cancelar(42, new CancelarEventoRequest("Se pospuso la boda", null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indique si se retiene, se devuelve todo o se devuelve una parte");
    }

    @Test
    @DisplayName("Devolver una parte registra el monto; no puede ser todo lo pagado")
    void cancelarDevolviendoParte() {
        comoAdministrador();
        pagos("4000.00", "2000.00");

        assertThatThrownBy(() -> eventoService.cancelar(42,
                new CancelarEventoRequest("Se pospuso", "DEVUELTO_PARCIAL", new BigDecimal("2000.00"))))
                .isInstanceOf(BusinessException.class);

        EventoResponse r = eventoService.cancelar(42,
                new CancelarEventoRequest("Se pospuso la boda", "DEVUELTO_PARCIAL", new BigDecimal("1000.00")));

        assertThat(r.estadoNombre()).isEqualTo("CANCELADO");
        assertThat(r.motivoCancelacion()).isEqualTo("Se pospuso la boda");
        assertThat(r.acuerdoAnticipo()).isEqualTo("DEVUELTO_PARCIAL");
        assertThat(r.montoDevuelto()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Sin pagos, cancelar solo pide el motivo")
    void cancelarSinPagos() {
        comoAdministrador();
        pagos("4000.00", "0");

        EventoResponse r = eventoService.cancelar(42, new CancelarEventoRequest("El cliente no confirmó", null, null));

        assertThat(r.acuerdoAnticipo()).isEqualTo("SIN_ANTICIPO");
    }

    @Test
    @DisplayName("Un evento se agenda con al menos 7 días de anticipación")
    void unaSemanaDeAnticipacion() {
        when(clienteRepository.findById(2)).thenReturn(Optional.of(evento.getCliente()));
        evento.getCliente().setEstado(EventoAsignacionesTest.estado("ACTIVO"));
        when(tipoEventoRepository.findById(1)).thenReturn(Optional.of(evento.getTipoEvento()));
        when(ubicacionRepository.findById(1)).thenReturn(Optional.of(evento.getUbicacion()));

        assertThatThrownBy(() -> eventoService.crear(new EventoRequest(null, 2, 1, 1,
                LocalDate.now().plusDays(6), LocalTime.of(12, 0), null, 80, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("al menos 7 días de anticipación");
    }
}
