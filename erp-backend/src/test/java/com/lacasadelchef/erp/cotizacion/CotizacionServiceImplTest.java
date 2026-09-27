package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionRequest;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Cotizacion;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.repository.ClienteRepository;
import com.lacasadelchef.erp.repository.CotizacionRepository;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.DetalleCotizacionRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.ServicioCotizacionRepository;
import com.lacasadelchef.erp.repository.TipoEventoRepository;
import com.lacasadelchef.erp.repository.UbicacionRepository;
import com.lacasadelchef.erp.repository.MenuPlatoRepository;
import jakarta.persistence.EntityManager;
import com.lacasadelchef.erp.entity.DetalleCotizacion;
import com.lacasadelchef.erp.entity.Menu;
import com.lacasadelchef.erp.entity.MenuPlato;
import com.lacasadelchef.erp.entity.Plato;
import com.lacasadelchef.erp.entity.TipoEvento;
import com.lacasadelchef.erp.entity.Ubicacion;
import com.lacasadelchef.erp.entity.id.MenuPlatoId;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CotizacionServiceImplTest {

    @Mock private CotizacionRepository cotizacionRepository;
    @Mock private CotizacionVersionRepository cotizacionVersionRepository;
    @Mock private DetalleCotizacionRepository detalleCotizacionRepository;
    @Mock private ServicioCotizacionRepository servicioCotizacionRepository;
    @Mock private MenuPlatoRepository menuPlatoRepository;
    @Mock private EntityManager entityManager;
    @Mock private ClienteRepository clienteRepository;
    @Mock private TipoEventoRepository tipoEventoRepository;
    @Mock private UbicacionRepository ubicacionRepository;
    @Mock private EstadoRepository estadoRepository;
    @InjectMocks private CotizacionServiceImpl cotizacionService;

    private static Estado estado(String nombre) {
        Estado estado = new Estado();
        estado.setIdEstado(1);
        estado.setNombre(nombre);
        return estado;
    }

    private static Cliente clienteActivo() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1);
        cliente.setNombre("Boda Perez");
        cliente.setEstado(estado("ACTIVO"));
        return cliente;
    }

    private static Cotizacion cotizacion() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setIdCotizacion(10);
        cotizacion.setCliente(clienteActivo());
        return cotizacion;
    }

    private static CotizacionVersion version(int numero, String estado) {
        CotizacionVersion version = new CotizacionVersion();
        version.setIdCotizacionVersion(100 + numero);
        version.setNumeroVersion(numero);
        version.setEstado(estado(estado));
        return version;
    }

    private static CotizacionRequest peticion(LocalDate fechaEvento) {
        return new CotizacionRequest(1, 1, 1, 150, fechaEvento, null, null);
    }

    @Test
    @DisplayName("Crear cotizacion con fecha de evento en el pasado se rechaza")
    void crearConFechaPasada() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteActivo()));

        assertThatThrownBy(() -> cotizacionService.crear(peticion(LocalDate.now().minusDays(1))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pasado");
        verify(cotizacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Editar los datos generales de una cotizacion ya enviada se rechaza")
    void actualizarEnviadaSeRechaza() {
        when(cotizacionRepository.findById(10)).thenReturn(Optional.of(cotizacion()));
        when(cotizacionVersionRepository.findByCotizacionIdCotizacionOrderByNumeroVersionDesc(10))
                .thenReturn(List.of(version(1, "ENVIADA")));

        assertThatThrownBy(() -> cotizacionService.actualizar(10, peticion(LocalDate.now().plusDays(30))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya fue enviada");
        verify(cotizacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Eliminar una cotizacion que ya se envio se rechaza")
    void eliminarEnviadaSeRechaza() {
        when(cotizacionRepository.findById(10)).thenReturn(Optional.of(cotizacion()));
        when(cotizacionVersionRepository.existsByCotizacionIdCotizacionAndEstadoNombreNot(10, "CREADA")).thenReturn(true);

        assertThatThrownBy(() -> cotizacionService.eliminar(10))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nunca se envio");
        verify(cotizacionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Eliminar un borrador (todas sus versiones en CREADA) borra tambien sus versiones")
    void eliminarBorrador() {
        Cotizacion cotizacion = cotizacion();
        CotizacionVersion version = version(1, "CREADA");
        when(cotizacionRepository.findById(10)).thenReturn(Optional.of(cotizacion));
        when(cotizacionVersionRepository.existsByCotizacionIdCotizacionAndEstadoNombreNot(10, "CREADA")).thenReturn(false);
        when(cotizacionVersionRepository.findByCotizacionIdCotizacionOrderByNumeroVersionDesc(10)).thenReturn(List.of(version));

        cotizacionService.eliminar(10);

        verify(cotizacionVersionRepository).delete(version);
        verify(cotizacionRepository).delete(cotizacion);
    }

    @Test
    @DisplayName("Si el borrador pasa de 80 a 120 personas, sus platos bajan al precio desde 100")
    void cruzarLas100PersonasReajustaPrecios() {
        Cotizacion cotizacion = cotizacion();
        cotizacion.setCantidadPersonas(80);
        CotizacionVersion borrador = version(1, "CREADA");
        Menu menu = new Menu();
        menu.setIdMenu(11);
        Plato lomo = new Plato();
        lomo.setIdPlato(31);
        DetalleCotizacion linea = new DetalleCotizacion();
        linea.setMenu(menu);
        linea.setPlato(lomo);
        linea.setPrecioUnitario(new BigDecimal("45.00"));
        MenuPlato catalogo = new MenuPlato();
        catalogo.setPlato(lomo);
        catalogo.setPrecioUnitario(new BigDecimal("45.00"));
        catalogo.setPrecioDesde100(new BigDecimal("40.00"));

        when(cotizacionRepository.findById(10)).thenReturn(Optional.of(cotizacion));
        when(cotizacionVersionRepository.findByCotizacionIdCotizacionOrderByNumeroVersionDesc(10))
                .thenReturn(List.of(borrador));
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cotizacion.getCliente()));
        when(tipoEventoRepository.findById(1)).thenReturn(Optional.of(new TipoEvento()));
        when(ubicacionRepository.findById(1)).thenReturn(Optional.of(new Ubicacion()));
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(detalleCotizacionRepository.findByCotizacionVersionIdCotizacionVersion(borrador.getIdCotizacionVersion()))
                .thenReturn(List.of(linea));
        when(menuPlatoRepository.findById(new MenuPlatoId(11, 31))).thenReturn(Optional.of(catalogo));

        CotizacionRequest a120 = new CotizacionRequest(1, 1, 1, 120, LocalDate.now().plusDays(30), null, null);
        cotizacionService.actualizar(10, a120);

        assertThat(linea.getPrecioUnitario()).isEqualByComparingTo("40.00");
    }

    @Test
    @DisplayName("Una hora de inicio fuera de los turnos del servicio (13:30) se rechaza")
    void horaFueraDeTurnoSeRechaza() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(clienteActivo()));
        when(tipoEventoRepository.findById(1)).thenReturn(Optional.of(new TipoEvento()));
        when(ubicacionRepository.findById(1)).thenReturn(Optional.of(new Ubicacion()));
        CotizacionRequest aLas1330 = new CotizacionRequest(1, 1, 1, 80, LocalDate.now().plusDays(30), null,
                LocalTime.of(13, 30));

        assertThatThrownBy(() -> cotizacionService.crear(aLas1330))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("entre las 11:00 y las 19:00");
        verify(cotizacionRepository, never()).save(any());
    }
}
