package com.lacasadelchef.erp.administracion.rbac;

import com.lacasadelchef.erp.administracion.rbac.dto.RolOpcionRequest;
import com.lacasadelchef.erp.administracion.rbac.dto.RolOpcionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles/{idRol}/permisos")
@RequiredArgsConstructor
public class RolOpcionController {

    private final RolOpcionService rolOpcionService;

    @GetMapping
    public List<RolOpcionResponse> listar(@PathVariable Integer idRol) {
        return rolOpcionService.listar(idRol);
    }

    @PostMapping("/{idOpcion}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/roles', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public RolOpcionResponse asignar(@PathVariable Integer idRol, @PathVariable Integer idOpcion,
                                     @Valid @RequestBody RolOpcionRequest request) {
        return rolOpcionService.asignar(idRol, idOpcion, request);
    }

    @PutMapping("/{idOpcion}")
    @PreAuthorize("@permisoService.tienePermiso('/api/roles', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public RolOpcionResponse actualizar(@PathVariable Integer idRol, @PathVariable Integer idOpcion,
                                        @Valid @RequestBody RolOpcionRequest request) {
        return rolOpcionService.actualizar(idRol, idOpcion, request);
    }

    @DeleteMapping("/{idOpcion}")
    @PreAuthorize("@permisoService.tienePermiso('/api/roles', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> quitar(@PathVariable Integer idRol, @PathVariable Integer idOpcion) {
        rolOpcionService.quitar(idRol, idOpcion);
        return ResponseEntity.noContent().build();
    }
}
