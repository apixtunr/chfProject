package com.lacasadelchef.erp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class AutorizacionApi implements AuthorizationManager<RequestAuthorizationContext> {

    private static final String PAGINA_RENTABILIDAD = "/api/rentabilidad";

    private static final Set<String> ESCRITURAS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final PermisoService permisoService;

    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> autenticacion,
                                           RequestAuthorizationContext contexto) {
        String ruta = contexto.getRequest().getRequestURI();

        RecursosApi.Regla regla = RecursosApi.reglaDe(ruta).orElse(null);
        if (regla == null) {
            return new AuthorizationDecision(false);
        }
        if (regla.acceso() == RecursosApi.Acceso.PUBLICO) {
            return new AuthorizationDecision(true);
        }

        Authentication auth = autenticacion.get();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return new AuthorizationDecision(false);
        }
        if (regla.acceso() == RecursosApi.Acceso.AUTENTICADO) {
            return new AuthorizationDecision(true);
        }

        if (ESCRITURAS.contains(contexto.getRequest().getMethod().toUpperCase(Locale.ROOT))) {
            return new AuthorizationDecision(true);
        }

        if (regla.acceso() == RecursosApi.Acceso.MODULO_FINANCIERO) {
            return new AuthorizationDecision(
                    permisoService.tienePermiso(regla.paginaUrl(), TipoPermiso.ALTA)
                            || permisoService.puedeConsultar(PAGINA_RENTABILIDAD));
        }

        return new AuthorizationDecision(permisoService.puedeConsultar(regla.paginaUrl()));
    }
}
