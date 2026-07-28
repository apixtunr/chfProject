package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.GeneroRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.GeneroResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generos")
@RequiredArgsConstructor
public class GeneroController {

    private final GeneroService generoService;

    @GetMapping
    public List<GeneroResponse> listar() {
        return generoService.listar();
    }

    @GetMapping("/{id}")
    public GeneroResponse obtener(@PathVariable Integer id) {
        return generoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/generos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public GeneroResponse crear(@Valid @RequestBody GeneroRequest request) {
        return generoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/generos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public GeneroResponse actualizar(@PathVariable Integer id, @Valid @RequestBody GeneroRequest request) {
        return generoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/generos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        generoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
