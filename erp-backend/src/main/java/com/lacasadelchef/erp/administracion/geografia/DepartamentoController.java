package com.lacasadelchef.erp.administracion.geografia;

import com.lacasadelchef.erp.administracion.geografia.dto.DepartamentoRequest;
import com.lacasadelchef.erp.administracion.geografia.dto.DepartamentoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    @GetMapping
    public List<DepartamentoResponse> listar() {
        return departamentoService.listar();
    }

    @GetMapping("/{id}")
    public DepartamentoResponse obtener(@PathVariable Integer id) {
        return departamentoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/departamentos', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public DepartamentoResponse crear(@Valid @RequestBody DepartamentoRequest request) {
        return departamentoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/departamentos', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public DepartamentoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody DepartamentoRequest request) {
        return departamentoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/departamentos', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        departamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
