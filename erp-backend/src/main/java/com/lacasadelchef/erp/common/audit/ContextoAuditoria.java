package com.lacasadelchef.erp.common.audit;

import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.security.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class ContextoAuditoria {

    private static final String LOOPBACK_IPV6_LARGO = "0:0:0:0:0:0:0:1";
    private static final String LOOPBACK_IPV6_CORTO = "::1";
    private static final String LOOPBACK_IPV4 = "127.0.0.1";

    private ContextoAuditoria() {
    }

    static Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
            return principal.getUsuario();
        }
        return null;
    }

    static Integer idUsuarioActual() {
        Usuario usuario = usuarioActual();
        return usuario != null ? usuario.getIdUsuario() : null;
    }

    static String ipActual() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return ipDe(attrs.getRequest());
        }
        return null;
    }

    public static String ipDe(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String reenviada = request.getHeader("X-Forwarded-For");
        String ip = (reenviada != null && !reenviada.isBlank())
                ? reenviada.split(",")[0].trim()
                : request.getRemoteAddr();
        if (LOOPBACK_IPV6_LARGO.equals(ip) || LOOPBACK_IPV6_CORTO.equals(ip)) {
            return LOOPBACK_IPV4;
        }
        return ip;
    }
}
