package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.cotizacion.dto.CotizacionVersionResponse;
import com.lacasadelchef.erp.entity.Cotizacion;
import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.TipoEstado;
import com.lacasadelchef.erp.repository.CotizacionRepository;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.DetalleCotizacionRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.ServicioCotizacionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CotizacionVersionServiceImplTest {

    private static final int ID_VERSION = 101;
    private static final int ID_ENVIADA = 2;

    @Mock private CotizacionVersionRepository cotizacionVersionRepository;
    @Mock private CotizacionRepository cotizacionRepository;
    @Mock private DetalleCotizacionRepository detalleCotizacionRepository;
    @Mock private ServicioCotizacionRepository servicioCotizacionRepository;
    @Mock private EstadoRepository estadoRepository;
    @Mock private BitacoraMovimientoService bitacoraMovimientoService;
    @Mock private EntityManager entityManager;
    @InjectMocks private CotizacionVersionServiceImpl versionService;

    private static Estado estado(int id, String nombre) {
        TipoEstado tipo = new TipoEstado();
        tipo.setNombreTipo("COTIZACION");
        Estado estado = new Estado();
        estado.setIdEstado(id);
        estado.setNombre(nombre);
        estado.setTipoEstado(tipo);
        return estado;
    }

    /** Version en CREADA de una cotizacion con la fecha indicada y el total indicado. */
    private CotizacionVersion prepararCreada(LocalDate fechaEvento, String montoTotal) {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setIdCotizacion(10);
        cotizacion.setFechaEvento(fechaEvento);
        CotizacionVersion version = new CotizacionVersion();
        version.setIdCotizacionVersion(ID_VERSION);
        version.setCotizacion(cotizacion);
        version.setNumeroVersion(1);
        version.setEstado(estado(1, "CREADA"));
        version.setMontoTotal(new BigDecimal(montoTotal));
        when(cotizacionVersionRepository.findById(ID_VERSION)).thenReturn(Optional.of(version));
        when(estadoRepository.findById(ID_ENVIADA)).thenReturn(Optional.of(estado(ID_ENVIADA, "ENVIADA")));
        return version;
    }

    @Test
    @DisplayName("Enviar una cotizacion sin platos ni servicios se rechaza")
    void enviarVaciaSeRechaza() {
        prepararCreada(LocalDate.now().plusDays(30), "0");

        assertThatThrownBy(() -> versionService.cambiarEstado(ID_VERSION, ID_ENVIADA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("al menos un plato o servicio");
        verify(cotizacionVersionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Enviar una cotizacion con total en cero se rechaza")
    void enviarConTotalCeroSeRechaza() {
        prepararCreada(LocalDate.now().plusDays(30), "0");
        when(detalleCotizacionRepository.existsByCotizacionVersionIdCotizacionVersion(ID_VERSION)).thenReturn(true);

        assertThatThrownBy(() -> versionService.cambiarEstado(ID_VERSION, ID_ENVIADA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mayor a Q 0.00");
    }

    @Test
    @DisplayName("Enviar una cotizacion sin fecha de evento se rechaza")
    void enviarSinFechaSeRechaza() {
        prepararCreada(null, "1500.00");
        when(detalleCotizacionRepository.existsByCotizacionVersionIdCotizacionVersion(ID_VERSION)).thenReturn(true);

        assertThatThrownBy(() -> versionService.cambiarEstado(ID_VERSION, ID_ENVIADA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fecha del evento");
    }

    @Test
    @DisplayName("Enviar una cotizacion cuya fecha de evento ya paso se rechaza")
    void enviarConFechaPasadaSeRechaza() {
        prepararCreada(LocalDate.now().minusDays(1), "1500.00");
        when(detalleCotizacionRepository.existsByCotizacionVersionIdCotizacionVersion(ID_VERSION)).thenReturn(true);

        assertThatThrownBy(() -> versionService.cambiarEstado(ID_VERSION, ID_ENVIADA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya paso");
    }

    @Test
    @DisplayName("Enviar una cotizacion completa (solo con servicios) pasa a ENVIADA")
    void enviarCompleta() {
        prepararCreada(LocalDate.now(), "800.00");
        lenient().when(detalleCotizacionRepository.existsByCotizacionVersionIdCotizacionVersion(ID_VERSION)).thenReturn(false);
        when(servicioCotizacionRepository.existsByCotizacionVersionIdCotizacionVersion(ID_VERSION)).thenReturn(true);
        when(cotizacionVersionRepository.save(any(CotizacionVersion.class))).thenAnswer(inv -> inv.getArgument(0));

        CotizacionVersionResponse response = versionService.cambiarEstado(ID_VERSION, ID_ENVIADA);

        assertThat(response.estadoNombre()).isEqualTo("ENVIADA");
    }
}
