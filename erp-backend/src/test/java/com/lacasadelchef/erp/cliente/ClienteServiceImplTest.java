package com.lacasadelchef.erp.cliente;

import com.lacasadelchef.erp.cliente.dto.ClienteRequest;
import com.lacasadelchef.erp.cliente.dto.ClienteResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Departamento;
import com.lacasadelchef.erp.entity.Municipio;
import com.lacasadelchef.erp.repository.ClienteRepository;
import com.lacasadelchef.erp.repository.MunicipioRepository;
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

    private static final int ID_MUNICIPIO = 1;

    @Mock private ClienteRepository clienteRepository;
    @Mock private MunicipioRepository municipioRepository;
    @Mock private BitacoraMovimientoService bitacoraMovimientoService;
    @InjectMocks private ClienteServiceImpl clienteService;

    private static Municipio municipioDePrueba() {
        Departamento departamento = new Departamento();
        departamento.setIdDepartamento(1);
        departamento.setNombreDepartamento("Guatemala");
        Municipio municipio = new Municipio();
        municipio.setIdMunicipio(ID_MUNICIPIO);
        municipio.setNombreMunicipio("Guatemala");
        municipio.setDepartamento(departamento);
        return municipio;
    }

    private static ClienteRequest peticion(String nombre) {
        return new ClienteRequest(nombre, "a@b.com", "5555-5555", "1234567-8",
                "5a calle 3-20, zona 1", ID_MUNICIPIO);
    }

    /** El cliente ya guardado trae municipio porque la columna es obligatoria. */
    private static Cliente clienteGuardado(Integer id, String nombre) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(id);
        cliente.setNombre(nombre);
        cliente.setDireccion("5a calle 3-20, zona 1");
        cliente.setMunicipio(municipioDePrueba());
        return cliente;
    }

    @Test
    @DisplayName("Crear cliente: guarda, recorta espacios y registra bitacora INSERT")
    void crearCliente() {
        when(municipioRepository.findById(ID_MUNICIPIO)).thenReturn(Optional.of(municipioDePrueba()));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setIdCliente(7);
            return c;
        });

        ClienteResponse response = clienteService.crear(peticion("  Boda Perez  "));

        assertThat(response.idCliente()).isEqualTo(7);
        assertThat(response.nombre()).isEqualTo("Boda Perez");
        assertThat(response.direccion()).isEqualTo("5a calle 3-20, zona 1");
        assertThat(response.nombreMunicipio()).isEqualTo("Guatemala");
        verify(bitacoraMovimientoService).registrar(eq("cliente"), eq(7), eq(Operacion.INSERT));
    }

    @Test
    @DisplayName("Crear cliente con municipio inexistente lanza ResourceNotFoundException")
    void crearConMunicipioInexistente() {
        when(municipioRepository.findById(ID_MUNICIPIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.crear(peticion("Boda Perez")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Municipio");
    }

    @Test
    @DisplayName("Actualizar cliente inexistente lanza ResourceNotFoundException")
    void actualizarInexistente() {
        when(clienteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.actualizar(99, peticion("X")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Eliminar cliente existente: borra y registra bitacora DELETE")
    void eliminarCliente() {
        Cliente cliente = clienteGuardado(5, "Eventos GT");
        when(clienteRepository.findById(5)).thenReturn(Optional.of(cliente));

        clienteService.eliminar(5);

        verify(clienteRepository).delete(cliente);
        verify(bitacoraMovimientoService).registrar(eq("cliente"), eq(5), eq(Operacion.DELETE));
    }

    @Test
    @DisplayName("Obtener por id devuelve el cliente mapeado a DTO")
    void obtenerPorId() {
        when(clienteRepository.findById(3)).thenReturn(Optional.of(clienteGuardado(3, "Colegio San Jose")));

        ClienteResponse response = clienteService.obtenerPorId(3);

        assertThat(response.idCliente()).isEqualTo(3);
        assertThat(response.nombre()).isEqualTo("Colegio San Jose");
        assertThat(response.nombreDepartamento()).isEqualTo("Guatemala");
    }
}
