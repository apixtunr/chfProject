package com.lacasadelchef.erp.security.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record LoginResponse(
        String token,
        String username,
        String nombreCompleto,
        String rol,
        List<PermisoResponse> permisos
) {
}
