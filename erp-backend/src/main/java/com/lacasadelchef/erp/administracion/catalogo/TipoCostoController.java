package com.lacasadelchef.erp.administracion.catalogo;

import com.lacasadelchef.erp.administracion.catalogo.dto.TipoCostoRequest;
import com.lacasadelchef.erp.administracion.catalogo.dto.TipoCostoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-costo")
@RequiredArgsConstructor
public class TipoCostoController {

    private final TipoCostoService tipoCostoService;

    @GetMapping
    public List<TipoCostoResponse> listar() {
        return tipoCostoService.listar();
    }

    @GetMapping("/{id}")
    public TipoCostoResponse obtener(@PathVariable Integer id) {
        return tipoCostoService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-costo', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public TipoCostoResponse crear(@Valid @RequestBody TipoCostoRequest request) {
        return tipoCostoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-costo', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public TipoCostoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TipoCostoRequest request) {
        return tipoCostoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-costo', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tipoCostoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
