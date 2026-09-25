package com.lacasadelchef.erp.cliente;

import com.lacasadelchef.erp.cliente.dto.ClienteRequest;
import com.lacasadelchef.erp.cliente.dto.ClienteResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.audit.Operacion;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.common.exception.ResourceNotFoundException;
import com.lacasadelchef.erp.entity.Cliente;
import com.lacasadelchef.erp.entity.Departamento;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Municipio;
import com.lacasadelchef.erp.repository.ClienteRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.MunicipioRepository;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    private static final int ID_MUNICIPIO = 1;
    /** NIT valido: 6769359 con digito verificador 8. */
    private static final String NIT_VALIDO = "6769359-8";

    @Mock private ClienteRepository clienteRepository;
    @Mock private MunicipioRepository municipioRepository;
    @Mock private EstadoRepository estadoRepository;
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

    private static Estado estado(int id, String nombre) {
        Estado estado = new Estado();
        estado.setIdEstado(id);
        estado.setNombre(nombre);
        return estado;
    }

    private static ClienteRequest peticion(String nombre, String nit, String telefono, String correo) {
        return new ClienteRequest(nombre, correo, telefono, nit, "5a calle 3-20, zona 1", ID_MUNICIPIO);
    }

    private static ClienteRequest peticion(String nombre) {
        return peticion(nombre, NIT_VALIDO, "5555-5555", "a@b.com");
    }

    /** El cliente ya guardado trae municipio y estado porque las columnas son obligatorias. */
    private static Cliente clienteGuardado(Integer id, String nombre, boolean activo) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(id);
        cliente.setNombre(nombre);
        cliente.setNit(NIT_VALIDO);
        cliente.setDireccion("5a calle 3-20, zona 1");
        cliente.setMunicipio(municipioDePrueba());
        cliente.setEstado(activo ? estado(1, "ACTIVO") : estado(2, "INACTIVO"));
        return cliente;
    }

    private void prepararAlta() {
        when(estadoRepository.findByTipoEstadoNombreTipoAndNombre("GENERAL", "ACTIVO"))
                .thenReturn(Optional.of(estado(1, "ACTIVO")));
        when(municipioRepository.findById(ID_MUNICIPIO)).thenReturn(Optional.of(municipioDePrueba()));
    }

    // ------------------------------------------------------------------- alta

    @Test
    @DisplayName("Crear cliente: guarda activo, recorta espacios y registra bitacora INSERT")
    void crearCliente() {
        prepararAlta();
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setIdCliente(7);
            return c;
        });

        ClienteResponse response = clienteService.crear(peticion("  Boda   Perez  "));

        assertThat(response.idCliente()).isEqualTo(7);
        assertThat(response.nombre()).isEqualTo("Boda Perez");
        assertThat(response.activo()).isTrue();
        assertThat(response.nombreMunicipio()).isEqualTo("Guatemala");
        verify(bitacoraMovimientoService).registrar(eq("cliente"), eq(7), eq(Operacion.INSERT));
    }

    @Test
    @DisplayName("Crear cliente: el NIT se guarda en formato canonico y el correo en minusculas")
    void crearNormalizaNitYCorreo() {
        prepararAlta();
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = clienteService.crear(peticion("Ana", "6769359 8", null, "Ana@Correo.COM"));

        assertThat(response.nit()).isEqualTo(NIT_VALIDO);
        assertThat(response.correo()).isEqualTo("ana@correo.com");
        assertThat(response.telefono()).isNull();
    }

    @Test
    @DisplayName("Crear cliente sin NIT lo registra como CF (consumidor final)")
    void crearSinNitEsConsumidorFinal() {
        prepararAlta();
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = clienteService.crear(peticion("Ana", "  ", "5555-5555", null));

        assertThat(response.nit()).isEqualTo("CF");
        verify(clienteRepository, never()).findByNit(any());
    }

    @Test
    @DisplayName("Crear cliente con NIT de digito verificador incorrecto se rechaza")
    void crearConNitInvalido() {
        prepararAlta();

        assertThatThrownBy(() -> clienteService.crear(peticion("Ana", "6769359-7", "5555-5555", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no es válido");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear cliente con NIT ya registrado se rechaza y dice de quien es")
    void crearConNitRepetido() {
        prepararAlta();
        when(clienteRepository.findByNit(NIT_VALIDO)).thenReturn(Optional.of(clienteGuardado(3, "Colegio San Jose", true)));

        assertThatThrownBy(() -> clienteService.crear(peticion("Otro")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Colegio San Jose");
    }

    @Test
    @DisplayName("Crear cliente con el NIT de uno inactivo sugiere reactivarlo")
    void crearConNitDeClienteInactivo() {
        prepararAlta();
        when(clienteRepository.findByNit(NIT_VALIDO)).thenReturn(Optional.of(clienteGuardado(3, "Colegio San Jose", false)));

        assertThatThrownBy(() -> clienteService.crear(peticion("Otro")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("reactívelo");
    }

    @Test
    @DisplayName("Crear cliente con un telefono ya registrado se rechaza, aunque este escrito distinto")
    void crearConTelefonoRepetido() {
        prepararAlta();
        when(clienteRepository.buscarPorTelefono("55555555", 0))
                .thenReturn(java.util.List.of(clienteGuardado(3, "Juan Domingo Salvador", true)));

        assertThatThrownBy(() -> clienteService.crear(peticion("Otro", "CF", "5555 5555", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("teléfono")
                .hasMessageContaining("Juan Domingo Salvador");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear cliente con un correo ya registrado se rechaza, sin importar mayusculas")
    void crearConCorreoRepetido() {
        prepararAlta();
        when(clienteRepository.buscarPorCorreo("ana@correo.com", 0))
                .thenReturn(java.util.List.of(clienteGuardado(3, "Ana Lucia", true)));

        assertThatThrownBy(() -> clienteService.crear(peticion("Otra", "CF", null, "ANA@correo.com")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("correo")
                .hasMessageContaining("Ana Lucia");
    }

    @Test
    @DisplayName("Crear cliente sin telefono ni correo se rechaza")
    void crearSinContacto() {
        prepararAlta();

        assertThatThrownBy(() -> clienteService.crear(peticion("Ana", NIT_VALIDO, " ", "")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("medio de contacto");
    }

    @Test
    @DisplayName("Crear cliente con municipio inexistente lanza ResourceNotFoundException")
    void crearConMunicipioInexistente() {
        when(estadoRepository.findByTipoEstadoNombreTipoAndNombre("GENERAL", "ACTIVO"))
                .thenReturn(Optional.of(estado(1, "ACTIVO")));
        when(municipioRepository.findById(ID_MUNICIPIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.crear(peticion("Boda Perez")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Municipio");
    }

    // ----------------------------------------------------------------- edicion

    @Test
    @DisplayName("Actualizar cliente conservando su propio NIT no cuenta como repetido")
    void actualizarConSuPropioNit() {
        Cliente cliente = clienteGuardado(5, "Eventos GT", true);
        when(clienteRepository.findById(5)).thenReturn(Optional.of(cliente));
        when(municipioRepository.findById(ID_MUNICIPIO)).thenReturn(Optional.of(municipioDePrueba()));
        when(clienteRepository.findByNit(NIT_VALIDO)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = clienteService.actualizar(5, peticion("Eventos GT S.A."));

        assertThat(response.nombre()).isEqualTo("Eventos GT S.A.");
    }

    @Test
    @DisplayName("Actualizar cliente inexistente lanza ResourceNotFoundException")
    void actualizarInexistente() {
        when(clienteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.actualizar(99, peticion("X")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------------------------------------------------- inactivar / reactivar

    @Test
    @DisplayName("Inactivar cliente sin negocio abierto: pasa a INACTIVO y no se borra")
    void inactivarSinNegocioAbierto() {
        Cliente cliente = clienteGuardado(5, "Eventos GT", true);
        when(clienteRepository.findById(5)).thenReturn(Optional.of(cliente));
        when(clienteRepository.contarCotizacionesAbiertas(5)).thenReturn(0L);
        when(clienteRepository.contarEventosVigentes(5)).thenReturn(0L);
        when(clienteRepository.saldoPendiente(5)).thenReturn(BigDecimal.ZERO);
        when(estadoRepository.findByTipoEstadoNombreTipoAndNombre("GENERAL", "INACTIVO"))
                .thenReturn(Optional.of(estado(2, "INACTIVO")));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = clienteService.cambiarEstado(5, false);

        assertThat(response.activo()).isFalse();
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Inactivar cliente con negocio abierto se rechaza y dice que tiene pendiente")
    void inactivarConNegocioAbierto() {
        when(clienteRepository.findById(5)).thenReturn(Optional.of(clienteGuardado(5, "Eventos GT", true)));
        when(clienteRepository.contarCotizacionesAbiertas(5)).thenReturn(0L);
        when(clienteRepository.contarEventosVigentes(5)).thenReturn(1L);
        when(clienteRepository.saldoPendiente(5)).thenReturn(new BigDecimal("1750.00"));

        assertThatThrownBy(() -> clienteService.cambiarEstado(5, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("1 evento sin terminar")
                .hasMessageContaining("pendientes de cobro");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reactivar cliente no revisa negocio abierto")
    void reactivar() {
        when(clienteRepository.findById(5)).thenReturn(Optional.of(clienteGuardado(5, "Eventos GT", false)));
        when(estadoRepository.findByTipoEstadoNombreTipoAndNombre("GENERAL", "ACTIVO"))
                .thenReturn(Optional.of(estado(1, "ACTIVO")));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = clienteService.cambiarEstado(5, true);

        assertThat(response.activo()).isTrue();
        verify(clienteRepository, never()).contarEventosVigentes(anyInt());
    }

    // ------------------------------------------------------------------ consulta

    @Test
    @DisplayName("Obtener por id devuelve el cliente mapeado a DTO")
    void obtenerPorId() {
        when(clienteRepository.findById(3)).thenReturn(Optional.of(clienteGuardado(3, "Colegio San Jose", true)));

        ClienteResponse response = clienteService.obtenerPorId(3);

        assertThat(response.idCliente()).isEqualTo(3);
        assertThat(response.nombre()).isEqualTo("Colegio San Jose");
        assertThat(response.nombreDepartamento()).isEqualTo("Guatemala");
    }
}
