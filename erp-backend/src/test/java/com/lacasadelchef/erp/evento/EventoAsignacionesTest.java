package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.EventoEmpleado;
import com.lacasadelchef.erp.entity.EventoVehiculo;
import com.lacasadelchef.erp.entity.Vehiculo;
import com.lacasadelchef.erp.entity.id.EventoEmpleadoId;
import com.lacasadelchef.erp.evento.dto.EventoEmpleadoRequest;
import com.lacasadelchef.erp.evento.dto.EventoVehiculoRequest;
import com.lacasadelchef.erp.repository.DocumentoEmpleadoRepository;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.EventoEmpleadoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.EventoVehiculoRepository;
import com.lacasadelchef.erp.repository.VehiculoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventoAsignacionesTest {

    private static final LocalDate SABADO = LocalDate.of(2026, 12, 12);

    static Estado estado(String nombre) {
        Estado estado = new Estado();
        estado.setNombre(nombre);
        return estado;
    }

    static Evento evento(int id, String estado, int desde, int hasta) {
        Evento evento = new Evento();
        evento.setIdEvento(id);
        evento.setEstado(estado(estado));
        evento.setFechaEvento(SABADO);
        evento.setHoraInicio(LocalTime.of(desde, 0));
        evento.setHoraFin(LocalTime.of(hasta, 0));
        return evento;
    }

    static Empleado empleado(String estado) {
        Empleado empleado = new Empleado();
        empleado.setIdEmpleado(5);
        empleado.setNombre("Lucía");
        empleado.setApellido("Ramírez");
        empleado.setEstado(estado(estado));
        return empleado;
    }

    @Test
    @DisplayName("Dos turnos se cruzan si uno empieza antes de que termine el otro; seguidos no")
    void cruceDeTurnos() {
        assertThat(EventoReglas.seCruzan(LocalTime.of(11, 0), LocalTime.of(15, 0), LocalTime.of(14, 0), LocalTime.of(18, 0))).isTrue();
        assertThat(EventoReglas.seCruzan(LocalTime.of(11, 0), LocalTime.of(15, 0), LocalTime.of(15, 0), LocalTime.of(19, 0))).isFalse();
        assertThat(EventoReglas.seCruzan(LocalTime.of(11, 0), null, LocalTime.of(18, 0), LocalTime.of(22, 0))).isTrue();
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class Personal {

        @Mock private EventoEmpleadoRepository eventoEmpleadoRepository;
        @Mock private EventoRepository eventoRepository;
        @Mock private EmpleadoRepository empleadoRepository;
        @Mock private EstadoRepository estadoRepository;
        @Mock private EventoVehiculoRepository eventoVehiculoRepository;
        @InjectMocks private EventoEmpleadoServiceImpl service;

        private final EventoEmpleadoRequest request =
                new EventoEmpleadoRequest(new BigDecimal("150"), null, null, 1);

        private void preparar(Evento evento, Empleado empleado) {
            when(eventoRepository.findById(evento.getIdEvento())).thenReturn(Optional.of(evento));
            when(empleadoRepository.findById(5)).thenReturn(Optional.of(empleado));
            lenient().when(estadoRepository.findById(1)).thenReturn(Optional.of(estado("ACTIVO")));
        }

        @Test
        @DisplayName("Un empleado inactivo no se asigna")
        void inactivo() {
            preparar(evento(1, "CREADO", 11, 15), empleado("INACTIVO"));

            assertThatThrownBy(() -> service.asignar(1, 5, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Lucía Ramírez está inactivo y no se puede asignar a un evento");
        }

        @Test
        @DisplayName("La misma persona no va a dos eventos que se cruzan el mismo día")
        void mismoTurnoEnOtroEvento() {
            Empleado lucia = empleado("ACTIVO");
            preparar(evento(1, "CREADO", 14, 18), lucia);
            EventoEmpleado enOtro = new EventoEmpleado();
            enOtro.setEvento(evento(2, "PLANIFICADO", 11, 15));
            enOtro.setEmpleado(lucia);
            when(eventoEmpleadoRepository.otrasAsignacionesDelDia(5, 1, SABADO)).thenReturn(List.of(enOtro));

            assertThatThrownBy(() -> service.asignar(1, 5, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Lucía Ramírez ya está asignado al evento #2 del mismo día (de 11:00 a 15:00)");
            verify(eventoEmpleadoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Si los horarios no se cruzan, sí se asigna a los dos eventos del día")
        void turnosSeguidos() {
            Empleado lucia = empleado("ACTIVO");
            preparar(evento(1, "CREADO", 15, 19), lucia);
            EventoEmpleado enOtro = new EventoEmpleado();
            enOtro.setEvento(evento(2, "PLANIFICADO", 11, 15));
            when(eventoEmpleadoRepository.otrasAsignacionesDelDia(5, 1, SABADO)).thenReturn(List.of(enOtro));
            when(eventoEmpleadoRepository.save(any(EventoEmpleado.class))).thenAnswer(inv -> inv.getArgument(0));

            assertThat(service.asignar(1, 5, request)).isNotNull();
        }

        @Test
        @DisplayName("A un evento finalizado ya no se le asigna personal")
        void eventoFinalizado() {
            preparar(evento(1, "FINALIZADO", 11, 15), empleado("ACTIVO"));

            assertThatThrownBy(() -> service.asignar(1, 5, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("No se puede asignar personal a un evento FINALIZADO");
        }

        @Test
        @DisplayName("No se quita del personal a quien conduce un vehículo del evento")
        void quitarConductor() {
            Evento evento = evento(1, "CREADO", 11, 15);
            Empleado lucia = empleado("ACTIVO");
            EventoEmpleado asignado = new EventoEmpleado();
            asignado.setEvento(evento);
            asignado.setEmpleado(lucia);
            when(eventoEmpleadoRepository.findById(new EventoEmpleadoId(1, 5))).thenReturn(Optional.of(asignado));
            Vehiculo hiace = new Vehiculo();
            hiace.setPlaca("P-123ABC");
            EventoVehiculo conduce = new EventoVehiculo();
            conduce.setVehiculo(hiace);
            conduce.setEmpleado(lucia);
            when(eventoVehiculoRepository.findByEventoIdEvento(1)).thenReturn(List.of(conduce));

            assertThatThrownBy(() -> service.quitar(1, 5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("conduce el vehículo P-123ABC");
            verify(eventoEmpleadoRepository, never()).delete(any());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class Vehiculos {

        @Mock private EventoVehiculoRepository eventoVehiculoRepository;
        @Mock private EventoRepository eventoRepository;
        @Mock private VehiculoRepository vehiculoRepository;
        @Mock private EmpleadoRepository empleadoRepository;
        @Mock private EventoEmpleadoRepository eventoEmpleadoRepository;
        @Mock private DocumentoEmpleadoRepository documentoEmpleadoRepository;
        @InjectMocks private EventoVehiculoServiceImpl service;

        private Vehiculo vehiculo(String estado) {
            Vehiculo vehiculo = new Vehiculo();
            vehiculo.setIdVehiculo(3);
            vehiculo.setPlaca("P-123ABC");
            vehiculo.setEstado(estado(estado));
            return vehiculo;
        }

        @Test
        @DisplayName("Un vehículo en mantenimiento no se asigna")
        void enMantenimiento() {
            when(eventoRepository.findById(1)).thenReturn(Optional.of(evento(1, "CREADO", 11, 15)));
            when(vehiculoRepository.findById(3)).thenReturn(Optional.of(vehiculo("EN MANTENIMIENTO")));

            assertThatThrownBy(() -> service.asignar(1, 3, new EventoVehiculoRequest(null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("El vehículo P-123ABC está EN MANTENIMIENTO y no se puede asignar");
        }

        @Test
        @DisplayName("Un vehículo hace un solo evento por día")
        void mismoDia() {
            when(eventoRepository.findById(1)).thenReturn(Optional.of(evento(1, "CREADO", 18, 22)));
            when(vehiculoRepository.findById(3)).thenReturn(Optional.of(vehiculo("DISPONIBLE")));
            EventoVehiculo enOtro = new EventoVehiculo();
            enOtro.setEvento(evento(2, "PLANIFICADO", 11, 15));
            when(eventoVehiculoRepository.otrasAsignacionesDelDia(3, 1, SABADO)).thenReturn(List.of(enOtro));

            assertThatThrownBy(() -> service.asignar(1, 3, new EventoVehiculoRequest(null)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("El vehículo P-123ABC ya está asignado al evento #2 del mismo día");
        }

        @Test
        @DisplayName("El conductor tiene que estar en el personal del evento")
        void conductorFueraDelPersonal() {
            when(eventoRepository.findById(1)).thenReturn(Optional.of(evento(1, "CREADO", 11, 15)));
            when(vehiculoRepository.findById(3)).thenReturn(Optional.of(vehiculo("DISPONIBLE")));
            when(empleadoRepository.findById(5)).thenReturn(Optional.of(empleado("ACTIVO")));
            when(eventoEmpleadoRepository.existsById(new EventoEmpleadoId(1, 5))).thenReturn(false);

            assertThatThrownBy(() -> service.asignar(1, 3, new EventoVehiculoRequest(5)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("no está en el personal del evento");
            verify(eventoVehiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Un conductor del personal del evento sí se asigna")
        void conductorDelPersonal() {
            when(eventoRepository.findById(1)).thenReturn(Optional.of(evento(1, "CREADO", 11, 15)));
            when(vehiculoRepository.findById(3)).thenReturn(Optional.of(vehiculo("DISPONIBLE")));
            when(empleadoRepository.findById(5)).thenReturn(Optional.of(empleado("ACTIVO")));
            when(eventoEmpleadoRepository.existsById(new EventoEmpleadoId(1, 5))).thenReturn(true);
            when(documentoEmpleadoRepository.existsByEmpleadoIdEmpleadoAndTipoDocumentoNombreTipo(5, "Licencia de conducir"))
                    .thenReturn(true);
            when(eventoVehiculoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var respuesta = service.asignar(1, 3, new EventoVehiculoRequest(5));

            assertThat(respuesta.idVehiculo()).isEqualTo(3);
        }

        @Test
        @DisplayName("Sin licencia de conducir registrada no se puede asignar como conductor")
        void conductorSinLicencia() {
            when(eventoRepository.findById(1)).thenReturn(Optional.of(evento(1, "CREADO", 11, 15)));
            when(vehiculoRepository.findById(3)).thenReturn(Optional.of(vehiculo("DISPONIBLE")));
            when(empleadoRepository.findById(5)).thenReturn(Optional.of(empleado("ACTIVO")));
            when(eventoEmpleadoRepository.existsById(new EventoEmpleadoId(1, 5))).thenReturn(true);
            when(documentoEmpleadoRepository.existsByEmpleadoIdEmpleadoAndTipoDocumentoNombreTipo(5, "Licencia de conducir"))
                    .thenReturn(false);

            assertThatThrownBy(() -> service.asignar(1, 3, new EventoVehiculoRequest(5)))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Lucía Ramírez no tiene licencia de conducir registrada: agréguela en sus documentos"
                            + " (Empleados) para que pueda conducir");
            verify(eventoVehiculoRepository, never()).save(any());
        }
    }
}
