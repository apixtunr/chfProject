package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.inventario.dto.CategoriaProductoRequest;
import com.lacasadelchef.erp.inventario.dto.CategoriaProductoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias-producto")
@RequiredArgsConstructor
public class CategoriaProductoController {

    private final CategoriaProductoService categoriaProductoService;

    @GetMapping
    public List<CategoriaProductoResponse> listar() {
        return categoriaProductoService.listar();
    }

    @GetMapping("/{id}")
    public CategoriaProductoResponse obtener(@PathVariable Integer id) {
        return categoriaProductoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/categorias-producto', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public CategoriaProductoResponse crear(@Valid @RequestBody CategoriaProductoRequest request) {
        return categoriaProductoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/categorias-producto', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public CategoriaProductoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody CategoriaProductoRequest request) {
        return categoriaProductoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/categorias-producto', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        categoriaProductoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
