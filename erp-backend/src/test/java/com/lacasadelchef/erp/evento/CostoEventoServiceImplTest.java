package com.lacasadelchef.erp.evento;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.CostoEvento;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.TipoCosto;
import com.lacasadelchef.erp.evento.dto.CostoEventoRequest;
import com.lacasadelchef.erp.repository.CostoEventoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.PagoRepository;
import com.lacasadelchef.erp.repository.TipoCostoRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Corregir un costo extra: lo que ya pago el cliente no se puede descuadrar. */
@ExtendWith(MockitoExtension.class)
class CostoEventoServiceImplTest {

    @Mock private CostoEventoRepository costoEventoRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private TipoCostoRepository tipoCostoRepository;
    @Mock private PagoRepository pagoRepository;
    @InjectMocks private CostoEventoServiceImpl service;

    private CostoEvento transporteDe100() {
        Evento evento = new Evento();
        evento.setIdEvento(45);
        TipoCosto transporte = new TipoCosto();
        transporte.setIdTipoCosto(2);
        transporte.setNombreTipo("Transporte");
        CostoEvento costo = new CostoEvento();
        costo.setIdCostoEvento(12);
        costo.setEvento(evento);
        costo.setTipoCosto(transporte);
        costo.setMonto(new BigDecimal("100.00"));
        when(costoEventoRepository.findById(12)).thenReturn(Optional.of(costo));
        return costo;
    }

    @Test
    @DisplayName("Si el cliente ya pago el costo, su monto no se puede cambiar")
    void cobradoNoCambiaMonto() {
        transporteDe100();
        when(pagoRepository.existsByCostoEventoIdCostoEventoAndEstadoNombreNot(12, "ANULADO")).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar(45, 12,
                new CostoEventoRequest(2, "Flete", new BigDecimal("150.00"), LocalDate.of(2026, 10, 15))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("anule primero ese pago");
        verify(costoEventoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ya cobrado, si se puede corregir la descripcion o la fecha sin tocar el monto")
    void cobradoCorrigeDescripcion() {
        CostoEvento costo = transporteDe100();
        when(pagoRepository.existsByCostoEventoIdCostoEventoAndEstadoNombreNot(12, "ANULADO")).thenReturn(true);
        when(tipoCostoRepository.findById(2)).thenReturn(Optional.of(costo.getTipoCosto()));
        when(costoEventoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = service.actualizar(45, 12,
                new CostoEventoRequest(2, "Flete del equipo", new BigDecimal("100.0"), LocalDate.of(2026, 10, 15)));

        assertThat(respuesta.descripcion()).isEqualTo("Flete del equipo");
        assertThat(respuesta.pagado()).isTrue();
    }

    @Test
    @DisplayName("Sin pago del cliente, el monto se corrige libremente")
    void sinCobroCambiaMonto() {
        CostoEvento costo = transporteDe100();
        when(pagoRepository.existsByCostoEventoIdCostoEventoAndEstadoNombreNot(12, "ANULADO")).thenReturn(false);
        when(tipoCostoRepository.findById(2)).thenReturn(Optional.of(costo.getTipoCosto()));
        when(costoEventoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = service.actualizar(45, 12,
                new CostoEventoRequest(2, null, new BigDecimal("150.00"), LocalDate.of(2026, 10, 15)));

        assertThat(respuesta.monto()).isEqualByComparingTo("150.00");
    }
}
