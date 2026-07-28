package com.lacasadelchef.erp.administracion.flota;

import com.lacasadelchef.erp.administracion.flota.dto.TipoPlacaRequest;
import com.lacasadelchef.erp.administracion.flota.dto.TipoPlacaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-placa")
@RequiredArgsConstructor
public class TipoPlacaController {

    private final TipoPlacaService tipoPlacaService;

    @GetMapping
    public List<TipoPlacaResponse> listar() {
        return tipoPlacaService.listar();
    }

    @GetMapping("/{id}")
    public TipoPlacaResponse obtener(@PathVariable Integer id) {
        return tipoPlacaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-placa', T(com.lacasadelchef.erp.security.TipoPermiso).ALTA)")
    public TipoPlacaResponse crear(@Valid @RequestBody TipoPlacaRequest request) {
        return tipoPlacaService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-placa', T(com.lacasadelchef.erp.security.TipoPermiso).MODIFICACION)")
    public TipoPlacaResponse actualizar(@PathVariable Integer id, @Valid @RequestBody TipoPlacaRequest request) {
        return tipoPlacaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permisoService.tienePermiso('/api/tipos-placa', T(com.lacasadelchef.erp.security.TipoPermiso).BAJA)")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        tipoPlacaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
