package com.lacasadelchef.erp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        if (jwtService.esValido(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String username = jwtService.extraerUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Revocacion por logout: rechazar tokens emitidos antes de la marca
            if (estaRevocado(token, userDetails)) {
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Se crea un contexto nuevo en vez de modificar el que ya estaba. Es el patron
            // que recomienda Spring Security desde que el contexto se resuelve de forma
            // diferida: al mutar el compartido, algunos filtros posteriores pueden seguir
            // viendo el contexto vacio y tratar la peticion como no autenticada.
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(auth);
            SecurityContextHolder.setContext(contexto);
        }

        filterChain.doFilter(request, response);
    }

    private boolean estaRevocado(String token, UserDetails userDetails) {
        if (!(userDetails instanceof UsuarioPrincipal principal)) {
            return false;
        }
        LocalDateTime validosDesde = principal.getUsuario().getTokensValidosDesde();
        if (validosDesde == null) {
            return false;
        }
        Instant marca = validosDesde.atZone(ZoneId.systemDefault()).toInstant();
        return jwtService.extraerFechaEmision(token).isBefore(marca);
    }
}
