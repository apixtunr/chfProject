package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.menu.dto.MenuPlatoRequest;
import com.lacasadelchef.erp.menu.dto.MenuPlatoResponse;
import com.lacasadelchef.erp.menu.dto.MenuRequest;
import com.lacasadelchef.erp.menu.dto.MenuResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public Page<MenuResponse> listar(@RequestParam(required = false) String nombre,
                                     @PageableDefault(size = 20, sort = "nombreMenu") Pageable pageable) {
        return menuService.listar(nombre, pageable);
    }

    @GetMapping("/{id}")
    public MenuResponse obtener(@PathVariable Integer id) {
        return menuService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/menus', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public MenuResponse crear(@Valid @RequestBody MenuRequest request) {
        return menuService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/menus', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public MenuResponse actualizar(@PathVariable Integer id, @Valid @RequestBody MenuRequest request) {
        return menuService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/menus', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        menuService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/platos")
    public List<MenuPlatoResponse> listarPlatos(@PathVariable Integer id) {
        return menuService.listarPlatos(id);
    }

    @PostMapping("/{id}/platos/{idPlato}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/menus', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public MenuPlatoResponse agregarPlato(@PathVariable Integer id, @PathVariable Integer idPlato,
                                          @Valid @RequestBody MenuPlatoRequest request) {
        return menuService.agregarPlato(id, idPlato, request);
    }

    @PutMapping("/{id}/platos/{idPlato}")
    @PreAuthorize("@permisoService.tienePermiso('/api/menus', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public MenuPlatoResponse actualizarPlato(@PathVariable Integer id, @PathVariable Integer idPlato,
                                             @Valid @RequestBody MenuPlatoRequest request) {
        return menuService.actualizarPlato(id, idPlato, request);
    }

    @DeleteMapping("/{id}/platos/{idPlato}")
    @PreAuthorize("@permisoService.tienePermiso('/api/menus', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> quitarPlato(@PathVariable Integer id, @PathVariable Integer idPlato) {
        menuService.quitarPlato(id, idPlato);
        return ResponseEntity.noContent().build();
    }
}
