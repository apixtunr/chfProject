package com.lacasadelchef.erp.ubicacion;

import com.lacasadelchef.erp.ubicacion.dto.UbicacionRequest;
import com.lacasadelchef.erp.ubicacion.dto.UbicacionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ubicaciones")
@RequiredArgsConstructor
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping
    public List<UbicacionResponse> listar() {
        return ubicacionService.listar();
    }

    @GetMapping("/{id}")
    public UbicacionResponse obtener(@PathVariable Integer id) {
        return ubicacionService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/ubicaciones', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public UbicacionResponse crear(@Valid @RequestBody UbicacionRequest request) {
        return ubicacionService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/ubicaciones', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public UbicacionResponse actualizar(@PathVariable Integer id, @Valid @RequestBody UbicacionRequest request) {
        return ubicacionService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/ubicaciones', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        ubicacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
