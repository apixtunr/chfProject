package com.lacasadelchef.erp.security;

import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.config.AppProperties;
import com.lacasadelchef.erp.entity.Accion;
import com.lacasadelchef.erp.entity.BitacoraAcceso;
import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.AccionRepository;
import com.lacasadelchef.erp.repository.BitacoraAccesoRepository;
import com.lacasadelchef.erp.repository.RolOpcionRepository;
import com.lacasadelchef.erp.repository.UsuarioRepository;
import com.lacasadelchef.erp.security.dto.LoginRequest;
import com.lacasadelchef.erp.security.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final int MAX_INTENTOS = 5;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolOpcionRepository rolOpcionRepository;
    @Mock private AccionRepository accionRepository;
    @Mock private BitacoraAccesoRepository bitacoraAccesoRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private AuthService authService;
    private Usuario usuario;
    private final MockHttpServletRequest http = new MockHttpServletRequest();

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties(
                new AppProperties.Security(new AppProperties.Security.Jwt("secreto", 60), MAX_INTENTOS),
                new AppProperties.Cors(List.of()));
        authService = new AuthService(usuarioRepository, rolOpcionRepository, accionRepository,
                bitacoraAccesoRepository, passwordEncoder, jwtService, props);

        Estado activo = new Estado();
        activo.setNombre("ACTIVO");
        Rol rol = new Rol();
        rol.setNombreRol("ADMINISTRADOR");

        usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setPasswordHash("$hash$");
        usuario.setIntentosAcceso(0);
        usuario.setEstado(activo);
        usuario.setRol(rol);

        // La bitacora se registra en casi todos los caminos; lenient para los que no.
        lenient().when(accionRepository.findByNombre(anyString()))
                .thenReturn(Optional.of(new Accion()));
    }

    @Test
    @DisplayName("Login exitoso: devuelve token, reinicia intentos y registra bitacora")
    void loginExitoso() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password", "$hash$")).thenReturn(true);
        when(jwtService.generarToken(eq("admin"), eq("ADMINISTRADOR"), anyString())).thenReturn("token-jwt");
        usuario.setIntentosAcceso(3);

        LoginResponse response = authService.login(new LoginRequest("admin", "password"), http);

        assertThat(response.token()).isEqualTo("token-jwt");
        assertThat(response.rol()).isEqualTo("ADMINISTRADOR");
        assertThat(usuario.getIntentosAcceso()).isZero();
        assertThat(usuario.getFechaUltimoAcceso()).isNotNull();
        verify(bitacoraAccesoRepository).save(any(BitacoraAcceso.class));
    }

    @Test
    @DisplayName("Usuario inexistente: BadCredentials sin revelar si existe o no")
    void usuarioInexistente() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("fantasma", "x"), http))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Password incorrecta: incrementa intentos_acceso y registra bitacora")
    void passwordIncorrectaIncrementaIntentos() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "$hash$")).thenReturn(false);
        usuario.setIntentosAcceso(2);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "mala"), http))
                .isInstanceOf(BadCredentialsException.class);

        assertThat(usuario.getIntentosAcceso()).isEqualTo(3);
        verify(bitacoraAccesoRepository).save(any(BitacoraAcceso.class));
    }

    @Test
    @DisplayName("Usuario con intentos agotados queda bloqueado, aun con password correcta")
    void usuarioBloqueadoPorIntentos() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        usuario.setIntentosAcceso(MAX_INTENTOS);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "password"), http))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("bloqueado");
    }

    @Test
    @DisplayName("Usuario inactivo no puede iniciar sesion")
    void usuarioInactivo() {
        usuario.getEstado().setNombre("INACTIVO");
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "password"), http))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactivo");
    }
}
