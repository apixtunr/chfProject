package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoDocumentoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoDocumentoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-documento")
@RequiredArgsConstructor
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    @GetMapping
    public List<TipoDocumentoResponse> listar() {
        return tipoDocumentoService.listar();
    }

    @GetMapping("/{id}")
    public TipoDocumentoResponse obtener(@PathVariable Integer id) {
        return tipoDocumentoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-documento', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public TipoDocumentoResponse crear(@Valid @RequestBody TipoDocumentoRequest request) {
        return tipoDocumentoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-documento', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public TipoDocumentoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TipoDocumentoRequest request) {
        return tipoDocumentoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-documento', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tipoDocumentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
