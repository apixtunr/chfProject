package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEventoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoEventoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-evento")
@RequiredArgsConstructor
public class TipoEventoController {

    private final TipoEventoService tipoEventoService;

    @GetMapping
    public List<TipoEventoResponse> listar() {
        return tipoEventoService.listar();
    }

    @GetMapping("/{id}")
    public TipoEventoResponse obtener(@PathVariable Integer id) {
        return tipoEventoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-evento', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public TipoEventoResponse crear(@Valid @RequestBody TipoEventoRequest request) {
        return tipoEventoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-evento', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public TipoEventoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TipoEventoRequest request) {
        return tipoEventoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-evento', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tipoEventoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
