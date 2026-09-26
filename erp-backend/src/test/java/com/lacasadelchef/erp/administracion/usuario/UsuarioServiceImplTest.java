package com.lacasadelchef.erp.administracion.usuario;

import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioActualizarRequest;
import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioRequest;
import com.lacasadelchef.erp.administracion.usuario.dto.UsuarioResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.RolRepository;
import com.lacasadelchef.erp.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * La relacion usuario-empleado es uno a uno: un empleado tiene como mucho un usuario, y
 * un usuario pertenece siempre a un empleado. La base lo garantiza con un indice unico y
 * un NOT NULL; estas pruebas cubren que el servicio lo diga con un mensaje entendible en
 * vez de dejar que reviente contra la base con un error generico.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    private static final int ID_ROL = 2;
    private static final int ID_ESTADO = 1;
    private static final int ID_EMPLEADO = 5;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private EstadoRepository estadoRepository;
    @Mock private EmpleadoRepository empleadoRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private BitacoraMovimientoService bitacoraMovimientoService;
    @InjectMocks private UsuarioServiceImpl usuarioService;

    private static UsuarioRequest alta(String username) {
        return new UsuarioRequest(username, "Secreta2026", ID_ROL, ID_ESTADO, ID_EMPLEADO);
    }

    private static Empleado empleado() {
        Empleado e = new Empleado();
        e.setIdEmpleado(ID_EMPLEADO);
        e.setNombre("Amado");
        e.setApellido("Soto Morales");
        return e;
    }

    private void catalogosDisponibles() {
        Rol rol = new Rol();
        rol.setIdRol(ID_ROL);
        rol.setNombreRol("OPERATIVO");
        Estado estado = new Estado();
        estado.setIdEstado(ID_ESTADO);
        estado.setNombre("ACTIVO");
        when(rolRepository.findById(ID_ROL)).thenReturn(Optional.of(rol));
        when(estadoRepository.findById(ID_ESTADO)).thenReturn(Optional.of(estado));
        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(Optional.of(empleado()));
    }

    @Test
    @DisplayName("Un usuario nuevo queda vinculado a su empleado")
    void creaVinculadoAlEmpleado() {
        when(usuarioRepository.existsByUsernameIgnoreCase("asoto")).thenReturn(false);
        when(usuarioRepository.findByEmpleadoIdEmpleado(ID_EMPLEADO)).thenReturn(Optional.empty());
        catalogosDisponibles();
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(7);
            return u;
        });

        UsuarioResponse resultado = usuarioService.crear(alta("asoto"));

        assertThat(resultado.idEmpleado()).isEqualTo(ID_EMPLEADO);
        assertThat(resultado.nombreEmpleado()).isEqualTo("Amado Soto Morales");
    }

    @Test
    @DisplayName("Un nombre de usuario repetido se rechaza nombrandolo, no con un error generico")
    void rechazaUsernameRepetido() {
        when(usuarioRepository.existsByUsernameIgnoreCase("admin")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crear(alta("admin")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("admin");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Un empleado no puede tener dos usuarios")
    void rechazaSegundoUsuarioParaElMismoEmpleado() {
        Usuario existente = new Usuario();
        existente.setIdUsuario(3);
        existente.setUsername("asoto");
        when(usuarioRepository.existsByUsernameIgnoreCase("asoto2")).thenReturn(false);
        when(usuarioRepository.findByEmpleadoIdEmpleado(ID_EMPLEADO)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> usuarioService.crear(alta("asoto2")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("asoto");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Al editar, el usuario no choca consigo mismo por su propio empleado")
    void editarNoChocaConsigoMismo() {
        Usuario propio = new Usuario();
        propio.setIdUsuario(7);
        propio.setUsername("asoto");
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(propio));
        when(usuarioRepository.findByEmpleadoIdEmpleado(ID_EMPLEADO)).thenReturn(Optional.of(propio));
        catalogosDisponibles();
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse resultado = usuarioService.actualizar(7,
                new UsuarioActualizarRequest(ID_ROL, ID_ESTADO, ID_EMPLEADO));

        assertThat(resultado.idEmpleado()).isEqualTo(ID_EMPLEADO);
    }
}
