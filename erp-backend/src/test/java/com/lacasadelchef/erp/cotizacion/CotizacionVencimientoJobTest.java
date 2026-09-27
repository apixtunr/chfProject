package com.lacasadelchef.erp.cotizacion;

import com.lacasadelchef.erp.entity.CotizacionVersion;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.repository.CotizacionVersionRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CotizacionVencimientoJobTest {

    @Mock private CotizacionVersionRepository cotizacionVersionRepository;
    @Mock private EstadoRepository estadoRepository;
    @InjectMocks private CotizacionVencimientoJob job;

    @Test
    @DisplayName("Las ENVIADAS con la vigencia terminada pasan a VENCIDA")
    void vencePasadas() {
        Estado enviada = new Estado();
        enviada.setNombre("ENVIADA");
        Estado vencida = new Estado();
        vencida.setNombre("VENCIDA");
        CotizacionVersion version = new CotizacionVersion();
        version.setEstado(enviada);
        when(cotizacionVersionRepository.findByEstadoNombreAndVigenteHastaBefore("ENVIADA", LocalDate.now()))
                .thenReturn(List.of(version));
        when(estadoRepository.findByTipoEstadoNombreTipoAndNombre("COTIZACION", "VENCIDA")).thenReturn(Optional.of(vencida));

        job.vencerCotizaciones();

        assertThat(version.getEstado().getNombre()).isEqualTo("VENCIDA");
        verify(cotizacionVersionRepository).saveAll(List.of(version));
    }

    @Test
    @DisplayName("Si ninguna vencio, no toca nada")
    void sinVencidas() {
        when(cotizacionVersionRepository.findByEstadoNombreAndVigenteHastaBefore("ENVIADA", LocalDate.now()))
                .thenReturn(List.of());

        job.vencerCotizaciones();

        verify(cotizacionVersionRepository, never()).saveAll(any());
    }
}
