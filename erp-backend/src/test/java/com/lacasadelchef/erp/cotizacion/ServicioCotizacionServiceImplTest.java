package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.cotizacion.dto.ServicioCotizacionRequest;
import com.lacasadelchef.erp.cotizacion.dto.ServicioCotizacionResponse;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.ServicioCotizacion;
import com.lacasadelchef.erp.entity.TipoServicio;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.ServicioCotizacionRepository;
import com.lacasadelchef.erp.repository.TipoServicioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioCotizacionServiceImplTest {

    private static final int ID_VERSION = 101;

    @Mock private ServicioCotizacionRepository servicioCotizacionRepository;
    @Mock private CotizacionVersionRepository cotizacionVersionRepository;
    @Mock private TipoServicioRepository tipoServicioRepository;
    @Mock private EntityManager entityManager;
    @InjectMocks private ServicioCotizacionServiceImpl servicioService;

    private CotizacionVersion version;

    @BeforeEach
    void versionEnBorrador() {
        Estado creada = new Estado();
        creada.setNombre("CREADA");
        CotizacionVersion version = new CotizacionVersion();
        version.setIdCotizacionVersion(ID_VERSION);
        version.setEstado(creada);
        this.version = version;
        lenient().when(cotizacionVersionRepository.findById(ID_VERSION)).thenReturn(Optional.of(version));
        lenient().when(servicioCotizacionRepository.save(any(ServicioCotizacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    private TipoServicio tipo(int id, String nombre, String precio) {
        TipoServicio tipo = new TipoServicio();
        tipo.setIdTipoServicio(id);
        tipo.setNombreTipo(nombre);
        tipo.setPrecioUnitario(precio == null ? null : new BigDecimal(precio));
        when(tipoServicioRepository.findById(id)).thenReturn(Optional.of(tipo));
        return tipo;
    }

    @Test
    @DisplayName("Hora extra de cocinero: 3 cocineros x 2 horas a Q25.00 son Q150.00, aunque se mande otro monto")
    void precioFijoPorCantidad() {
        tipo(6, "Hora extra de cocinero", "25.00");

        ServicioCotizacionResponse linea = servicioService.agregar(ID_VERSION,
                new ServicioCotizacionRequest(6, "3 cocineros x 2 horas", 6, new BigDecimal("999")));

        assertThat(linea.cantidad()).isEqualTo(6);
        assertThat(linea.precioUnitario()).isEqualByComparingTo("25.00");
        assertThat(linea.monto()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("Un servicio sin precio fijo usa el monto que se indique")
    void montoLibre() {
        tipo(2, "Personal de cocina adicional", null);

        ServicioCotizacionResponse linea = servicioService.agregar(ID_VERSION,
                new ServicioCotizacionRequest(2, "2 cocineros para la segunda entrada", null, new BigDecimal("1200")));

        assertThat(linea.cantidad()).isEqualTo(1);
        assertThat(linea.monto()).isEqualByComparingTo("1200");
    }

    @Test
    @DisplayName("Un servicio sin precio fijo y sin monto se rechaza")
    void montoLibreObligatorio() {
        tipo(2, "Personal de cocina adicional", null);

        assertThatThrownBy(() -> servicioService.agregar(ID_VERSION,
                new ServicioCotizacionRequest(2, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Indique el monto");
    }

    @Test
    @DisplayName("Un servicio que ya no se ofrece no se puede agregar")
    void tipoInactivoNoSeAgrega() {
        tipo(9, "Transporte", null).setActivo(false);

        assertThatThrownBy(() -> servicioService.agregar(ID_VERSION,
                new ServicioCotizacionRequest(9, null, null, new BigDecimal("300"))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("'Transporte' ya no se ofrece");
    }

    @Test
    @DisplayName("La linea que ya tenia un servicio desactivado se puede seguir editando")
    void tipoInactivoEnLineaExistente() {
        TipoServicio transporte = tipo(9, "Transporte", null);
        transporte.setActivo(false);
        ServicioCotizacion existente = new ServicioCotizacion();
        existente.setIdServicioCotizacion(50);
        existente.setCotizacionVersion(version);
        existente.setTipoServicio(transporte);
        when(servicioCotizacionRepository.findById(50)).thenReturn(Optional.of(existente));

        ServicioCotizacionResponse linea = servicioService.actualizar(ID_VERSION, 50,
                new ServicioCotizacionRequest(9, "Flete a Antigua", null, new BigDecimal("350")));

        assertThat(linea.monto()).isEqualByComparingTo("350");
    }
}
