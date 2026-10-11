package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.MenuModuloResponse;
import com.lacasadelchef.erp.security.UsuarioPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** El menu lateral del usuario que inicio sesion. */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuLateralController {

    private final MenuLateralService menuLateralService;

    @GetMapping
    public List<MenuModuloResponse> menu(@AuthenticationPrincipal UsuarioPrincipal principal) {
        return menuLateralService.menuDe(principal.getUsuario());
    }
}
