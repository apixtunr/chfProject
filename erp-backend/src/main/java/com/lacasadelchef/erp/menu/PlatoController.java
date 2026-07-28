package com.lacasadelchef.erp.menu;

import com.lacasadelchef.erp.menu.dto.PlatoRequest;
import com.lacasadelchef.erp.menu.dto.PlatoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platos")
@RequiredArgsConstructor
public class PlatoController {

    private final PlatoService platoService;

    @GetMapping
    public Page<PlatoResponse> listar(@RequestParam(required = false) String nombre,
                                      @PageableDefault(size = 20, sort = "nombrePlato") Pageable pageable) {
        return platoService.listar(nombre, pageable);
    }

    @GetMapping("/{id}")
    public PlatoResponse obtener(@PathVariable Integer id) {
        return platoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/platos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public PlatoResponse crear(@Valid @RequestBody PlatoRequest request) {
        return platoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/platos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public PlatoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody PlatoRequest request) {
        return platoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/platos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        platoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
