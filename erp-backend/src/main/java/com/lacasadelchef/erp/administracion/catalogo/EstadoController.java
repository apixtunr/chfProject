package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.EstadoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.EstadoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
public class EstadoController {

    private final EstadoService estadoService;

    @GetMapping
    public List<EstadoResponse> listar(@RequestParam(required = false) Integer idTipoEstado) {
        return estadoService.listar(idTipoEstado);
    }

    @GetMapping("/{id}")
    public EstadoResponse obtener(@PathVariable Integer id) {
        return estadoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/estados', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public EstadoResponse crear(@Valid @RequestBody EstadoRequest request) {
        return estadoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/estados', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public EstadoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody EstadoRequest request) {
        return estadoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/estados', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        estadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
