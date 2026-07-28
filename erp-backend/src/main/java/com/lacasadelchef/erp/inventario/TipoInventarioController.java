package com.lacasadelchef.erp.inventario;

import com.lacasadelchef.erp.inventario.dto.TipoInventarioRequest;
import com.lacasadelchef.erp.inventario.dto.TipoInventarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-inventario")
@RequiredArgsConstructor
public class TipoInventarioController {

    private final TipoInventarioService tipoInventarioService;

    @GetMapping
    public List<TipoInventarioResponse> listar() {
        return tipoInventarioService.listar();
    }

    @GetMapping("/{id}")
    public TipoInventarioResponse obtener(@PathVariable Integer id) {
        return tipoInventarioService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-inventario', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public TipoInventarioResponse crear(@Valid @RequestBody TipoInventarioRequest request) {
        return tipoInventarioService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-inventario', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public TipoInventarioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TipoInventarioRequest request) {
        return tipoInventarioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-inventario', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tipoInventarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
