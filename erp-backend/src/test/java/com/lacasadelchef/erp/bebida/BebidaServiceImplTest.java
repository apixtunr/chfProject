package com.lacasadelchef.erp.bebida;

import com.lacasadelchef.erp.bebida.dto.BebidaRequest;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Bebida;
import com.lacasadelchef.erp.repository.BebidaRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BebidaServiceImplTest {

    @Mock private BebidaRepository bebidaRepository;
    @Mock private EstadoRepository estadoRepository;
    @InjectMocks private BebidaServiceImpl bebidaService;

    @Test
    @DisplayName("No se crea una bebida con el nombre de otra, sin importar mayusculas ni espacios")
    void nombreRepetido() {
        when(bebidaRepository.existsByNombreBebidaIgnoreCase("Té frío")).thenReturn(true);

        assertThatThrownBy(() -> bebidaService.crear(new BebidaRequest("  Té frío ", 1)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ya existe la bebida 'Té frío'");
        verify(bebidaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Una bebida en uso no se borra: el mensaje dice donde se usa y que hacer")
    void enUsoNoSeBorra() {
        Bebida te = new Bebida();
        te.setIdBebida(1);
        te.setNombreBebida("Té frío");
        when(bebidaRepository.findById(1)).thenReturn(Optional.of(te));
        when(bebidaRepository.contarPlatos(1)).thenReturn(52L);
        when(bebidaRepository.contarLineas(1)).thenReturn(3L);

        assertThatThrownBy(() -> bebidaService.eliminar(1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("la incluyen 52 plato(s)")
                .hasMessageContaining("Inactívela");
        verify(bebidaRepository, never()).delete(any());
    }
}
