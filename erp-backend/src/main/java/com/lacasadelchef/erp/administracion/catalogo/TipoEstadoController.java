package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEstadoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEstadoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-estado")
@RequiredArgsConstructor
public class TipoEstadoController {

    private final TipoEstadoService tipoEstadoService;

    @GetMapping
    public List<TipoEstadoResponse> listar() {
        return tipoEstadoService.listar();
    }

    @GetMapping("/{id}")
    public TipoEstadoResponse obtener(@PathVariable Integer id) {
        return tipoEstadoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-estado', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public TipoEstadoResponse crear(@Valid @RequestBody TipoEstadoRequest request) {
        return tipoEstadoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-estado', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public TipoEstadoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TipoEstadoRequest request) {
        return tipoEstadoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-estado', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tipoEstadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
