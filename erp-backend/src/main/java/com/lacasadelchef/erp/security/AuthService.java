package com.lacasadelchef.erp.security;

import com.lacasadelchef.erp.common.audit.ContextoAuditoria;
import com.lacasadelchef.erp.common.exception.BusinessException;
import com.lacasadelchef.erp.config.AppProperties;
import com.lacasadelchef.erp.entity.Accion;
import com.lacasadelchef.erp.entity.BitacoraAcceso;
import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.repository.AccionRepository;
import com.lacasadelchef.erp.repository.BitacoraAccesoRepository;
import com.lacasadelchef.erp.repository.RolOpcionRepository;
import com.lacasadelchef.erp.repository.UsuarioRepository;
import com.lacasadelchef.erp.security.dto.LoginRequest;
import com.lacasadelchef.erp.security.dto.LoginResponse;
import com.lacasadelchef.erp.security.dto.PermisoResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String ACCION_LOGIN = "LOGIN";
    private static final String ACCION_LOGIN_FALLIDO = "LOGIN_FALLIDO";
    private static final String ACCION_LOGOUT = "LOGOUT";

    private final UsuarioRepository usuarioRepository;
    private final RolOpcionRepository rolOpcionRepository;
    private final AccionRepository accionRepository;
    private final BitacoraAccesoRepository bitacoraAccesoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    @Transactional(noRollbackFor = {BadCredentialsException.class, BusinessException.class})
    public LoginResponse login(LoginRequest request, HttpServletRequest http) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado().getNombre())) {
            registrarBitacora(usuario, ACCION_LOGIN_FALLIDO, http, "USUARIO_INACTIVO");
            throw new BusinessException("El usuario se encuentra inactivo o bloqueado");
        }

        if (usuario.getIntentosAcceso() >= appProperties.security().maxIntentosAcceso()) {
            registrarBitacora(usuario, ACCION_LOGIN_FALLIDO, http, "USUARIO_BLOQUEADO");
            throw new BusinessException(
                    "Usuario bloqueado por exceder los intentos de acceso. Contacte al administrador");
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            usuario.setIntentosAcceso(usuario.getIntentosAcceso() + 1);
            registrarBitacora(usuario, ACCION_LOGIN_FALLIDO, http, "PASSWORD_INCORRECTA");
            throw new BadCredentialsException("Credenciales invalidas");
        }

        usuario.setIntentosAcceso(0);
        usuario.setFechaUltimoAcceso(LocalDateTime.now());
        registrarBitacora(usuario, ACCION_LOGIN, http, "EXITOSO");

        String token = jwtService.generarToken(usuario.getUsername(), usuario.getRol().getNombreRol());

        return LoginResponse.builder()
                .token(token)
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getEmpleado() != null
                        ? usuario.getEmpleado().getNombre() + " " + usuario.getEmpleado().getApellido()
                        : usuario.getUsername())
                .rol(usuario.getRol().getNombreRol())
                .permisos(cargarPermisos(usuario.getRol().getIdRol()))
                .build();
    }

    @Transactional
    public void logout(HttpServletRequest http) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioPrincipal principal)) {
            return; 
        }
        Usuario usuario = usuarioRepository.findById(principal.getUsuario().getIdUsuario())
                .orElseThrow(() -> new IllegalStateException("Usuario de la sesion no existe"));
        usuario.setTokensValidosDesde(LocalDateTime.now());
        registrarBitacora(usuario, ACCION_LOGOUT, http, "EXITOSO");
    }

    private List<PermisoResponse> cargarPermisos(Integer idRol) {
        return rolOpcionRepository.findPermisosByRol(idRol).stream()
                .map(ro -> PermisoResponse.builder()
                        .modulo(ro.getOpcion().getMenuVista().getModulo().getNombre())
                        .menuVista(ro.getOpcion().getMenuVista().getNombre())
                        .opcion(ro.getOpcion().getNombreOpcion())
                        .paginaUrl(ro.getOpcion().getPaginaUrl())
                        .alta(ro.isAlta())
                        .baja(ro.isBaja())
                        .modificacion(ro.isModificacion())
                        .imprimir(ro.isImprimir())
                        .exportar(ro.isExportar())
                        .build())
                .toList();
    }

    private void registrarBitacora(Usuario usuario, String accion, HttpServletRequest http, String resultado) {
        Accion acc = accionRepository.findByNombre(accion)
                .orElseThrow(() -> new IllegalStateException("Accion no configurada: " + accion));
        BitacoraAcceso registro = new BitacoraAcceso();
        registro.setUsuario(usuario);
        registro.setAccion(acc);
        registro.setIpOrigen(ContextoAuditoria.ipDe(http));
        registro.setNavegador(http.getHeader("User-Agent"));
        registro.setResultado(resultado);
        bitacoraAccesoRepository.save(registro);
    }
}
