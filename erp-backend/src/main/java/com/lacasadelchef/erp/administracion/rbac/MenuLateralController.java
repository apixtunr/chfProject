package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.MenuModuloResponse;
import com.lacasadelchef.erp.security.AuthService;
import com.lacasadelchef.erp.security.UsuarioPrincipal;
import com.lacasadelchef.erp.security.dto.PermisoResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuLateralController {

    private final MenuLateralService menuLateralService;
    private final AuthService authService;

    @GetMapping
    public List<MenuModuloResponse> menu(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return menuLateralService.menuDe(principal.getUsuario());
    }

    @GetMapping("/permisos")
    public List<PermisoResponse> permisos(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return authService.permisosDe(principal.getUsuario());
    }
}
