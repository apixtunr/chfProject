package com.lacasadelchef.erp.pago;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Evento;
import com.lacasadelchef.erp.entity.Pago;
import com.lacasadelchef.erp.pago.dto.PagoRequest;
import com.lacasadelchef.erp.repository.CostoEventoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.EventoRepository;
import com.lacasadelchef.erp.repository.MetodoPagoRepository;
import com.lacasadelchef.erp.repository.PagoRepository;
import com.lacasadelchef.erp.repository.VPagoEventoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    @Mock private PagoRepository pagoRepository;
    @Mock private EventoRepository eventoRepository;
    @Mock private MetodoPagoRepository metodoPagoRepository;
    @Mock private EstadoRepository estadoRepository;
    @Mock private CostoEventoRepository costoEventoRepository;
    @Mock private VPagoEventoRepository vPagoEventoRepository;
    @InjectMocks private PagoServiceImpl service;

    @Test
    @DisplayName("A un evento cancelado ya no se le registran pagos: lo pagado se resolvio en el acuerdo")
    void eventoCancelado() {
        Estado cancelado = new Estado();
        cancelado.setNombre("CANCELADO");
        Evento evento = new Evento();
        evento.setIdEvento(44);
        evento.setEstado(cancelado);
        Pago pago = new Pago();
        pago.setIdPago(1);
        when(pagoRepository.findById(1)).thenReturn(Optional.of(pago));
        when(eventoRepository.findById(44)).thenReturn(Optional.of(evento));

        assertThatThrownBy(() -> service.actualizar(1,
                new PagoRequest(44, 1, new BigDecimal("500.00"), null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("El evento #44 está cancelado: ya no se le registran pagos");
        verify(pagoRepository, never()).save(any());
    }
}
