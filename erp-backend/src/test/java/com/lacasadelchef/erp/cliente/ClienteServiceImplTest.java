package com.lacasadelchef.erp.cliente;

import com.lacasadelchef.erp.cliente.dto.ClienteRequest;
import com.lacasadelchef.erp.cliente.dto.ClienteResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private BitacoraMovimientoService bitacoraMovimientoService;
    @InjectMocks private ClienteServiceImpl clienteService;

    @Test
    @DisplayName("Crear cliente: guarda, recorta espacios y registra bitacora INSERT")
    void crearCliente() {
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setIdCliente(7);
            return c;
        });

        ClienteResponse response = clienteService.crear(
                new ClienteRequest("  Boda Perez  ", "a@b.com", "5555-5555", "1234567-8"));

        assertThat(response.idCliente()).isEqualTo(7);
        assertThat(response.nombre()).isEqualTo("Boda Perez");
        verify(bitacoraMovimientoService).registrar(eq("cliente"), eq(7), eq(Operacion.INSERT));
    }

    @Test
    @DisplayName("Actualizar cliente inexistente lanza ResourceNotFoundException")
    void actualizarInexistente() {
        when(clienteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.actualizar(99,
                new ClienteRequest("X", null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Eliminar cliente existente: borra y registra bitacora DELETE")
    void eliminarCliente() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(5);
        cliente.setNombre("Eventos GT");
        when(clienteRepository.findById(5)).thenReturn(Optional.of(cliente));

        clienteService.eliminar(5);

        verify(clienteRepository).delete(cliente);
        verify(bitacoraMovimientoService).registrar(eq("cliente"), eq(5), eq(Operacion.DELETE));
    }

    @Test
    @DisplayName("Obtener por id devuelve el cliente mapeado a DTO")
    void obtenerPorId() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(3);
        cliente.setNombre("Colegio San Jose");
        when(clienteRepository.findById(3)).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.obtenerPorId(3);

        assertThat(response.idCliente()).isEqualTo(3);
        assertThat(response.nombre()).isEqualTo("Colegio San Jose");
    }
}
