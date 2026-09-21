package com.lacasadelchef.erp.common.audit;

import com.lacasadelchef.erp.entity.Usuario;
import com.lacasadelchef.erp.security.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Quien y desde donde: lo que toda fila de bitacora necesita y que solo existe en el
 * hilo de la peticion HTTP. Desde el scheduler (jobs de estado de evento) no hay usuario
 * ni peticion, y ambos quedan en null a proposito: el movimiento lo hizo el sistema.
 */
public final class ContextoAuditoria {

    /** Loopback IPv6 (::1 sin comprimir, que es como lo reporta el servidor) e IPv4. */
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

    /**
     * IP real del usuario. Si el sistema queda publicado detras de un proxy o balanceador
     * (nginx, IIS), getRemoteAddr() devuelve la direccion del proxy y no la de quien hizo
     * la accion; por eso se prefiere X-Forwarded-For, que es donde el proxy deja la IP
     * original. El loopback se normaliza a 127.0.0.1 porque el servidor lo reporta en
     * notacion IPv6 larga (0:0:0:0:0:0:0:1) cuando se entra desde la misma maquina.
     */
    public static String ipDe(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String reenviada = request.getHeader("X-Forwarded-For");
        String ip = (reenviada != null && !reenviada.isBlank())
                // Con varios proxies encadenados el header trae una lista: el primero es el cliente.
                ? reenviada.split(",")[0].trim()
                : request.getRemoteAddr();
        if (LOOPBACK_IPV6_LARGO.equals(ip) || LOOPBACK_IPV6_CORTO.equals(ip)) {
            return LOOPBACK_IPV4;
        }
        return ip;
    }
}
