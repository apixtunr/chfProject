package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    @Mock private ClienteRepository clienteRepository;
    @Mock private TipoEventoRepository tipoEventoRepository;
    @Mock private UbicacionRepository ubicacionRepository;
    @Mock private EstadoRepository estadoRepository;
    @Mock private BitacoraMovimientoService bitacoraMovimientoService;
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
        return new CotizacionRequest(1, 1, 1, 150, fechaEvento, null);
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
}
