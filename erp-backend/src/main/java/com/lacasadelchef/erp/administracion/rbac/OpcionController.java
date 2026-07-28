package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.OpcionRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.OpcionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opciones")
@RequiredArgsConstructor
public class OpcionController {

    private final OpcionService opcionService;

    @GetMapping
    public List<OpcionResponse> listar(@RequestParam(required = false) Integer idMenuVista) {
        return opcionService.listar(idMenuVista);
    }

    @GetMapping("/{id}")
    public OpcionResponse obtener(@PathVariable Integer id) {
        return opcionService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/opciones', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public OpcionResponse crear(@Valid @RequestBody OpcionRequest request) {
        return opcionService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/opciones', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public OpcionResponse actualizar(@PathVariable Integer id, @Valid @RequestBody OpcionRequest request) {
        return opcionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/opciones', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        opcionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
