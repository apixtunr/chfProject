package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.ModuloRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.ModuloResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modulos")
@RequiredArgsConstructor
public class ModuloController {

    private final ModuloService moduloService;

    @GetMapping
    public List<ModuloResponse> listar() {
        return moduloService.listar();
    }

    @GetMapping("/{id}")
    public ModuloResponse obtener(@PathVariable Integer id) {
        return moduloService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/modulos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public ModuloResponse crear(@Valid @RequestBody ModuloRequest request) {
        return moduloService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/modulos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public ModuloResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ModuloRequest request) {
        return moduloService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/modulos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        moduloService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
