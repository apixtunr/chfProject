package com.lacasadelchef.erp.security;

import com.lacasadelchef.erp.entity.RolOpcion;
import com.lacasadelchef.erp.repository.RolOpcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermisoService {

    private final RolOpcionRepository rolOpcionRepository;

    @Transactional(readOnly = true)
    public boolean tienePermiso(String paginaUrl, TipoPermiso permiso) {
        UsuarioPrincipal principal = usuarioActual();
        if (principal == null) {
            return false;
        }
        Integer idRol = principal.getUsuario().getRol().getIdRol();
        return rolOpcionRepository.findByRolIdRolAndOpcionPaginaUrl(idRol, paginaUrl)
                .map(ro -> evaluar(ro, permiso))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean puedeConsultar(String paginaUrl) {
        UsuarioPrincipal principal = usuarioActual();
        if (principal == null) {
            return false;
        }
        return rolOpcionRepository
                .findByRolIdRolAndOpcionPaginaUrl(principal.getUsuario().getRol().getIdRol(), paginaUrl)
                .isPresent();
    }

    private boolean evaluar(RolOpcion ro, TipoPermiso permiso) {
        return switch (permiso) {
            case ALTA -> ro.isAlta();
            case BAJA -> ro.isBaja();
            case MODIFICACION -> ro.isModificacion();
            case IMPRIMIR -> ro.isImprimir();
            case EXPORTAR -> ro.isExportar();
        };
    }

    private UsuarioPrincipal usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal;
        }
        return null;
    }
}
