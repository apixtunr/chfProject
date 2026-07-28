package com.lacasadelchef.erp.security;

import com.lacasadelchef.erp.entity.Estado;
import com.lacasadelchef.erp.entity.Rol;
import com.lacasadelchef.erp.entity.RolOpcion;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.RolOpcionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermisoServiceTest {

    private static final String URL = "/api/clientes";

    @Mock private RolOpcionRepository rolOpcionRepository;
    @InjectMocks private PermisoService permisoService;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("ADMINISTRADOR siempre tiene permiso, sin consultar rol_opcion")
    void adminSiempreTienePermiso() {
        autenticar("ADMINISTRADOR", 1);

        assertThat(permisoService.tienePermiso(URL, TipoPermiso.BAJA)).isTrue();
    }

    @Test
    @DisplayName("Rol con la bandera activa tiene permiso")
    void rolConBanderaActiva() {
        autenticar("OPERATIVO", 2);
        RolOpcion ro = new RolOpcion();
        ro.setAlta(true);
        when(rolOpcionRepository.findByRolIdRolAndOpcionPaginaUrl(2, URL))
                .thenReturn(Optional.of(ro));

        assertThat(permisoService.tienePermiso(URL, TipoPermiso.ALTA)).isTrue();
        assertThat(permisoService.tienePermiso(URL, TipoPermiso.BAJA)).isFalse();
    }

    @Test
    @DisplayName("Sin fila rol_opcion configurada, el permiso se niega (falla cerrado)")
    void sinConfiguracionSeNiega() {
        autenticar("OPERATIVO", 2);
        when(rolOpcionRepository.findByRolIdRolAndOpcionPaginaUrl(2, URL))
                .thenReturn(Optional.empty());

        assertThat(permisoService.tienePermiso(URL, TipoPermiso.ALTA)).isFalse();
    }

    @Test
    @DisplayName("Sin usuario autenticado, el permiso se niega")
    void sinAutenticacionSeNiega() {
        SecurityContextHolder.clearContext();

        assertThat(permisoService.tienePermiso(URL, TipoPermiso.ALTA)).isFalse();
    }

    private void autenticar(String nombreRol, Integer idRol) {
        Rol rol = new Rol();
        rol.setIdRol(idRol);
        rol.setNombreRol(nombreRol);
        Estado estado = new Estado();
        estado.setNombre("ACTIVO");
        Usuario usuario = new Usuario();
        usuario.setUsername("test");
        usuario.setRol(rol);
        usuario.setEstado(estado);

        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
