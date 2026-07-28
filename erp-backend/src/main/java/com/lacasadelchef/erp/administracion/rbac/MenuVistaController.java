package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.MenuVistaRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.MenuVistaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-vistas")
@RequiredArgsConstructor
public class MenuVistaController {

    private final MenuVistaService menuVistaService;

    @GetMapping
    public List<MenuVistaResponse> listar(@RequestParam(required = false) Integer idModulo) {
        return menuVistaService.listar(idModulo);
    }

    @GetMapping("/{id}")
    public MenuVistaResponse obtener(@PathVariable Integer id) {
        return menuVistaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/menu-vistas', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public MenuVistaResponse crear(@Valid @RequestBody MenuVistaRequest request) {
        return menuVistaService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/menu-vistas', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public MenuVistaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody MenuVistaRequest request) {
        return menuVistaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/menu-vistas', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        menuVistaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
