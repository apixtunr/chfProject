package com.lacasadelchef.erp.security;

import com.lacasadelchef.erp.entity.RolOpcion;
import com.lacasadelchef.erp.repository.RolOpcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autorizacion fina por rol_opcion, usada desde @PreAuthorize en los controladores.
 * ADMINISTRADOR siempre tiene permiso (evita el problema de arranque: sin esto nadie
 * podria configurar opcion/rol_opcion si el propio admin quedara bloqueado). Para
 * cualquier otro rol, si no existe una fila opcion.pagina_url + rol_opcion configurada,
 * se niega el permiso (falla cerrado).
 */
@Service
@RequiredArgsConstructor
public class PermisoService {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private final RolOpcionRepository rolOpcionRepository;

    @Transactional(readOnly = true)
    public boolean tienePermiso(String paginaUrl, TipoPermiso permiso) {
        UsuarioPrincipal principal = usuarioActual();
        if (principal == null) {
            return false;
        }
        if (ROL_ADMINISTRADOR.equalsIgnoreCase(principal.getUsuario().getRol().getNombreRol())) {
            return true;
        }
        Integer idRol = principal.getUsuario().getRol().getIdRol();
        return rolOpcionRepository.findByRolIdRolAndOpcionPaginaUrl(idRol, paginaUrl)
                .map(ro -> evaluar(ro, permiso))
                .orElse(false);
    }

    /**
     * Si el rol puede CONSULTAR esa pagina.
     *
     * No mira ninguna bandera: le alcanza con que exista la fila en rol_opcion. Esa es la
     * semantica que el modelo ya tenia y que nadie verificaba: una fila con alta, baja y
     * modificacion en falso significa "puede mirar, no puede tocar". Asi estan hoy COCINA
     * sobre /api/eventos y BODEGA sobre /api/tipos-inventario, y asi lo interpreta el
     * frontend desde siempre en su metodo puedeVer(). Por eso no hizo falta agregar un
     * permiso CONSULTA ni una columna a la tabla.
     *
     * Falla cerrado: sin fila, no se puede ver.
     */
    @Transactional(readOnly = true)
    public boolean puedeConsultar(String paginaUrl) {
        UsuarioPrincipal principal = usuarioActual();
        if (principal == null) {
            return false;
        }
        if (ROL_ADMINISTRADOR.equalsIgnoreCase(principal.getUsuario().getRol().getNombreRol())) {
            return true;
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
