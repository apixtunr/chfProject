package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Plato;
import com.lacasadelchef.erp.menu.dto.PlatoRequest;
import com.lacasadelchef.erp.menu.dto.PlatoResponse;
import com.lacasadelchef.erp.bebida.dto.BebidaResumen;
import com.lacasadelchef.erp.repository.BebidaRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.PlatoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.lacasadelchef.erp.menu.BebidaDelPlatoTest.JAMAICA;
import static com.lacasadelchef.erp.menu.BebidaDelPlatoTest.TE_FRIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatoServiceImplTest {

    @Mock private PlatoRepository platoRepository;
    @Mock private EstadoRepository estadoRepository;
    @Mock private BebidaRepository bebidaRepository;
    @InjectMocks private PlatoServiceImpl platoService;

    @Test
    @DisplayName("Al editar un plato quedan exactamente las bebidas elegidas: se quitan y se agregan")
    void sincronizaBebidas() {
        Estado activo = new Estado();
        activo.setIdEstado(1);
        activo.setNombre("ACTIVO");
        Plato milanesa = BebidaDelPlatoTest.plato("Milanesa de pollo gratinada con queso", TE_FRIO);
        milanesa.setEstado(activo);
        when(platoRepository.findById(7)).thenReturn(Optional.of(milanesa));
        when(estadoRepository.findById(1)).thenReturn(Optional.of(activo));
        when(bebidaRepository.findById(2)).thenReturn(Optional.of(JAMAICA));
        when(platoRepository.save(any(Plato.class))).thenAnswer(inv -> inv.getArgument(0));

        PlatoResponse respuesta = platoService.actualizar(7,
                new PlatoRequest("Milanesa de pollo gratinada con queso", 1, null, List.of(2)));

        assertThat(respuesta.bebidas()).extracting(BebidaResumen::nombreBebida).containsExactly("Rosa de Jamaica");
    }
}
