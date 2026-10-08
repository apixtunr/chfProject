package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.AccesoSistemaRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.DocumentoItemRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoResponse;
import com.lacasadelchef.erp.administracion.usuario.PoliticaUsuario;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.DocumentoEmpleado;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.PuestoEmpleado;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.entity.TipoDocumento;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.DocumentoEmpleadoRepository;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.GeneroRepository;
import com.lacasadelchef.erp.repository.PuestoEmpleadoRepository;
import com.lacasadelchef.erp.repository.RolRepository;
import com.lacasadelchef.erp.repository.TipoDocumentoRepository;
import com.lacasadelchef.erp.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El alta de empleado, con y sin acceso al sistema.
 *
 * Lo que mas importa comprobar aca es que empleado y usuario no se puedan separar: si el
 * usuario no se puede crear, el empleado tampoco tiene que quedar grabado. Sin eso, un
 * nombre de usuario repetido dejaria a la persona cargada a medias y el segundo intento
 * la duplicaria.
 */
@ExtendWith(MockitoExtension.class)
class EmpleadoServiceImplTest {

    private static final int ID_PUESTO = 1;
    private static final int ID_ESTADO = 1;
    private static final int ID_ROL = 2;

    @Mock private EmpleadoRepository empleadoRepository;
    @Mock private PuestoEmpleadoRepository puestoEmpleadoRepository;
    @Mock private EstadoRepository estadoRepository;
    @Mock private GeneroRepository generoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PoliticaUsuario politicaUsuario;
    @Mock private DocumentoEmpleadoRepository documentoEmpleadoRepository;
    @Mock private TipoDocumentoRepository tipoDocumentoRepository;
    @Mock private DocumentosEmpleado documentosEmpleado;
    @InjectMocks private EmpleadoServiceImpl empleadoService;

    private static final String DPI = "2547 12345 0101";
    private static final TipoDocumento TIPO_DPI = tipo(1, "DPI");
    private static final TipoDocumento TIPO_LICENCIA = tipo(2, "Licencia de conducir");

    private static TipoDocumento tipo(int id, String nombre) {
        TipoDocumento tipo = new TipoDocumento();
        tipo.setIdTipoDocumento(id);
        tipo.setNombreTipo(nombre);
        return tipo;
    }

    private static EmpleadoRequest pedido(AccesoSistemaRequest acceso) {
        return pedido(acceso, null);
    }

    private static EmpleadoRequest pedido(AccesoSistemaRequest acceso, List<DocumentoItemRequest> documentos) {
        return new EmpleadoRequest(ID_PUESTO, ID_ESTADO, null, "Amado", "Soto Morales",
                null, null, null, DPI, documentos, acceso);
    }

    private static DocumentoEmpleado documento(Empleado empleado, TipoDocumento tipo, String numero) {
        DocumentoEmpleado documento = new DocumentoEmpleado();
        documento.setEmpleado(empleado);
        documento.setTipoDocumento(tipo);
        documento.setNumeroDocumento(numero);
        return documento;
    }

    private void catalogosDisponibles() {
        PuestoEmpleado puesto = new PuestoEmpleado();
        puesto.setIdPuestoEmpleado(ID_PUESTO);
        puesto.setNombreRol("Administrador");
        Estado estado = new Estado();
        estado.setIdEstado(ID_ESTADO);
        estado.setNombre("ACTIVO");
        when(puestoEmpleadoRepository.findById(ID_PUESTO)).thenReturn(Optional.of(puesto));
        when(estadoRepository.findById(ID_ESTADO)).thenReturn(Optional.of(estado));
        lenient().when(estadoRepository.findByTipoEstadoNombreTipoAndNombre("GENERAL", "ACTIVO"))
                .thenReturn(Optional.of(estado));
    }

    private void documentosDisponibles() {
        lenient().when(documentosEmpleado.tipoDpi()).thenReturn(TIPO_DPI);
        lenient().when(tipoDocumentoRepository.findById(1)).thenReturn(Optional.of(TIPO_DPI));
        lenient().when(tipoDocumentoRepository.findById(2)).thenReturn(Optional.of(TIPO_LICENCIA));
    }

    private void empleadoSeGraba() {
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> {
            Empleado e = inv.getArgument(0);
            e.setIdEmpleado(99);
            return e;
        });
    }

    @Test
    @DisplayName("Sin acceso solo se crea el empleado; no se toca la tabla de usuarios")
    void sinAccesoNoCreaUsuario() {
        catalogosDisponibles();
        documentosDisponibles();
        empleadoSeGraba();

        EmpleadoResponse resultado = empleadoService.crear(pedido(null));

        assertThat(resultado.idUsuario()).isNull();
        assertThat(resultado.username()).isNull();
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Con acceso se crean el empleado y su usuario, vinculados entre si")
    void conAccesoCreaAmbos() {
        catalogosDisponibles();
        documentosDisponibles();
        empleadoSeGraba();
        Rol rol = new Rol();
        rol.setIdRol(ID_ROL);
        rol.setNombreRol("OPERATIVO");
        when(rolRepository.findById(ID_ROL)).thenReturn(Optional.of(rol));
        when(politicaUsuario.generarPara(any(Empleado.class))).thenReturn("amado.soto");
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        Usuario[] guardado = new Usuario[1];
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(7);
            guardado[0] = u;
            return u;
        });
        // La respuesta se arma releyendo el usuario del empleado, como al consultarlo.
        when(usuarioRepository.findByEmpleadoIdEmpleado(99)).thenAnswer(inv -> Optional.ofNullable(guardado[0]));

        EmpleadoResponse resultado = empleadoService.crear(
                pedido(new AccesoSistemaRequest("Secreta2026", ID_ROL)));

        assertThat(resultado.idUsuario()).isEqualTo(7);
        // El nombre de usuario no lo escribe nadie: sale del nombre del empleado.
        assertThat(resultado.username()).isEqualTo("amado.soto");
        assertThat(resultado.rolUsuario()).isEqualTo("OPERATIVO");

        // El usuario guardado apunta al empleado recien creado: la relacion queda armada
        // en la misma operacion, no en un segundo paso que podria no ocurrir.
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Al editar un empleado no se puede crear ni cambiar su acceso")
    void editarNoAdmiteAcceso() {
        assertThatThrownBy(() -> empleadoService.actualizar(1,
                pedido(new AccesoSistemaRequest("Secreta2026", ID_ROL))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuarios");

        verify(empleadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("El DPI se guarda junto con el empleado, en la misma operacion")
    void guardaElDpi() {
        catalogosDisponibles();
        documentosDisponibles();
        empleadoSeGraba();

        empleadoService.crear(pedido(null));

        verify(documentosEmpleado).guardar(any(Empleado.class), eq(TIPO_DPI), eq(DPI));
    }

    @Test
    @DisplayName("El DPI no se acepta dentro de la lista de documentos: va en los datos del empleado")
    void dpiEnDocumentosSeRechaza() {
        catalogosDisponibles();
        documentosDisponibles();
        empleadoSeGraba();

        assertThatThrownBy(() -> empleadoService.crear(pedido(null, List.of(new DocumentoItemRequest(1, DPI)))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("datos del empleado");
    }

    @Test
    @DisplayName("Un mismo tipo de documento no puede venir dos veces")
    void documentoRepetido() {
        catalogosDisponibles();
        documentosDisponibles();
        empleadoSeGraba();

        assertThatThrownBy(() -> empleadoService.crear(pedido(null, List.of(
                new DocumentoItemRequest(2, "A-123"), new DocumentoItemRequest(2, "B-456")))))
                .isInstanceOf(BusinessException.class)
                .hasMessage("El documento Licencia de conducir viene repetido");
    }

    @Test
    @DisplayName("Al editar, el documento que ya no viene se quita; el DPI nunca")
    void reemplazaDocumentos() {
        catalogosDisponibles();
        documentosDisponibles();
        Empleado existente = new Empleado();
        existente.setIdEmpleado(99);
        when(empleadoRepository.findById(99)).thenReturn(Optional.of(existente));
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));
        DocumentoEmpleado dpi = documento(existente, TIPO_DPI, "2547123450101");
        DocumentoEmpleado licencia = documento(existente, TIPO_LICENCIA, "A-123");
        when(documentoEmpleadoRepository.findByEmpleadoIdEmpleado(99)).thenReturn(List.of(dpi, licencia));

        empleadoService.actualizar(99, pedido(null, List.of()));

        verify(documentoEmpleadoRepository).delete(licencia);
        verify(documentoEmpleadoRepository, never()).delete(dpi);
    }
}
