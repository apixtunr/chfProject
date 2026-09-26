package com.lacasadelchef.erp.administracion.rrhh;

import com.lacasadelchef.erp.administracion.rrhh.dto.AccesoSistemaRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoRequest;
import com.lacasadelchef.erp.administracion.rrhh.dto.EmpleadoResponse;
import com.lacasadelchef.erp.common.audit.BitacoraMovimientoService;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.entity.Empleado;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.PuestoEmpleado;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.EmpleadoRepository;
import com.lacasadelchef.erp.repository.EstadoRepository;
import com.lacasadelchef.erp.repository.GeneroRepository;
import com.lacasadelchef.erp.repository.PuestoEmpleadoRepository;
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
    @Mock private BitacoraMovimientoService bitacoraMovimientoService;
    @InjectMocks private EmpleadoServiceImpl empleadoService;

    private static EmpleadoRequest pedido(AccesoSistemaRequest acceso) {
        return new EmpleadoRequest(ID_PUESTO, ID_ESTADO, null, "Amado", "Soto Morales",
                null, null, null, acceso);
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
        empleadoSeGraba();
        Rol rol = new Rol();
        rol.setIdRol(ID_ROL);
        rol.setNombreRol("OPERATIVO");
        when(rolRepository.findById(ID_ROL)).thenReturn(Optional.of(rol));
        when(usuarioRepository.existsByUsernameIgnoreCase("asoto")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(7);
            return u;
        });

        EmpleadoResponse resultado = empleadoService.crear(
                pedido(new AccesoSistemaRequest("asoto", "Secreta2026", ID_ROL)));

        assertThat(resultado.idUsuario()).isEqualTo(7);
        assertThat(resultado.username()).isEqualTo("asoto");
        assertThat(resultado.rolUsuario()).isEqualTo("OPERATIVO");

        // El usuario guardado apunta al empleado recien creado: la relacion queda armada
        // en la misma operacion, no en un segundo paso que podria no ocurrir.
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Si el nombre de usuario ya existe, no se graba tampoco el empleado")
    void usernameRepetidoNoDejaEmpleadoAMedias() {
        when(usuarioRepository.existsByUsernameIgnoreCase("asoto")).thenReturn(true);

        assertThatThrownBy(() -> empleadoService.crear(
                pedido(new AccesoSistemaRequest("asoto", "Secreta2026", ID_ROL))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("asoto");

        // Lo esencial: el empleado nunca llego a grabarse. Si se hubiera grabado antes de
        // detectar el choque, al reintentar quedarian dos empleados para la misma persona.
        verify(empleadoRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Al editar un empleado no se puede crear ni cambiar su acceso")
    void editarNoAdmiteAcceso() {
        assertThatThrownBy(() -> empleadoService.actualizar(1,
                pedido(new AccesoSistemaRequest("asoto", "Secreta2026", ID_ROL))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Usuarios");

        verify(empleadoRepository, never()).save(any());
    }
}
